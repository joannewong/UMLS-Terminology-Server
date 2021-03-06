// Content controller
tsApp
  .controller(
    'ContentCtrl',
    [
      '$rootScope',
      '$scope',
      '$routeParams',
      '$http',
      '$uibModal',
      '$location',
      '$q',
      '$anchorScroll',
      '$sce',
      '$uibModal',
      'gpService',
      'utilService',
      'tabService',
      'securityService',
      'metadataService',
      'contentService',
      'configureService',
      'websocketService',
      'appConfig',
      function($rootScope, $scope, $routeParams, $http, $uibModal, $location, $q, $anchorScroll,
        $sce, $uibModal, gpService, utilService, tabService, securityService, metadataService,
        contentService, configureService, websocketService, appConfig) {
        console.debug('configure ContentCtrl');

        if ($routeParams.mode == 'simple') {
          console.debug('  simple mode deletected, hide tabs');
          tabService.setShowing(false);
        } else {
          console.debug('  non-simple mode detected, show tabs');
          tabService.setShowing(true);
        }

        // retrieve the user
        $scope.user = securityService.getUser();
        console.debug($scope.user);
        // Clear error
        utilService.clearError();

        // pass app configuration constants to scope (for email link)
        $scope.appConfig = appConfig;

        // Handle resetting tabs on "back" and "reload" button, but also handles
        // non-standard
        // content modes which may not have tabs
        if (!$routeParams.mode) {
          tabService.setSelectedTabByLabel('Content');
        }

        //
        // Scope Variables
        //

        // Scope variables initialized from services
        // TODO Add this to other controllers where preferences are modified
        $scope.user = securityService.getUser();
        $scope.isGuestUser = securityService.isGuestUser;
        $scope.mode = $routeParams.mode === 'simple' ? 'simple' : 'full';
        $scope.metadata = metadataService.getModel();
        $scope.component = null;
        $scope.pageSizes = contentService.getPageSizes();

        // Search parameters
        $scope.searchParams = contentService.getSearchParams();
        $scope.searchResults = {};
        $scope.searchResultsCollapsed = true;
        $scope.searchOrBrowse = null;

        // favorites
        $scope.favoritesSearchParams = contentService.getSearchParams();

        // the expression constructor array
        $scope.expressions = [];
        $scope.selectedExpr = null;

        // set on terminology change
        $scope.autocompleteUrl = null;

        // Track search results and what type we are querying for
        $scope.queryForList = true;
        // whether to query for list, default
        $scope.queryForTree = false;
        // whether to query for tree

        // Variables for iterating through trees in report
        $scope.treeCount = null;
        $scope.treeViewed = null;
        $scope.componentTree = null;

        // component scoring
        $scope.scoreExcellent = 0.7;
        $scope.scoreGood = 0.3;

        $scope.getColorForScore = function(score) {
          if (score > $scope.scoreExcellent) {
            return 'green';
          } else if (score > $scope.scoreGood) {
            return 'yellow';
          } else {
            return 'orange';
          }
        };

        //
        // Watch expressions
        //

        // Watch for changes in metadata.terminologies (indicates application
        // readiness)
        $scope.$watch('metadata.terminology', function() {

          // clear the terminology-specific variables
          $scope.autoCompleteUrl = null;

          // if no terminology specified, stop
          if ($scope.metadata.terminology == null) {
            return;
          }

          // set the autocomplete url, with pattern:
          // /type/{terminology}/{version}/autocomplete/{searchTerm}
          $scope.autocompleteUrl = contentUrl
            + $scope.metadata.terminology.organizingClassType.toLowerCase() + '/'
            + $scope.metadata.terminology.terminology + '/' + $scope.metadata.terminology.version
            + "/autocomplete/";

        });

        // on route changes, save search params and last viewed component
        $scope.$on('$routeChangeStart', function() {
          contentService.setLastSearchParams($scope.searchParams);
          contentService.setLastComponent($scope.component);
        });

        //
        // General
        //

        // Configure tab and accordion
        $scope.configureTab = function() {
          $scope.user.userPreferences.lastTab = '/content';
          securityService.updateUserPreferences($scope.user.userPreferences);
        };

        // Sets the terminololgy
        $scope.setTerminology = function(terminology) {

          var deferred = $q.defer();

          metadataService.setTerminology(terminology).then(function() {

            // if metathesaurus, ensure list view set
            if (terminology.metathesaurus) {
              $scope.setListView();
            }
            // if a query is specified, research
            if ($scope.searchParams.query || $scope.searchParams.advancedMode) {
              $scope.findComponents(false, true);
            }

            if ($scope.user && $scope.user.userPreferences) {
              $scope.user.userPreferences.lastTerminology = terminology.terminology;
              securityService.updateUserPreferences($scope.user.userPreferences);
            }
            deferred.resolve();
          }, function() {
            deferred.reject();
          });

          return deferred.promise;
        };

        // Autocomplete function
        $scope.autocomplete = function(searchTerms) {
          // if invalid search terms, return empty array
          if (searchTerms == null || searchTerms == undefined || searchTerms.length < 3) {
            return new Array();
          }
          return contentService.autocomplete(searchTerms, $scope.autocompleteUrl);
        };

        // 
        // Search functions
        // 

        // Clear the search box and perform any additional operations
        // required
        $scope.clearQuery = function() {
          $scope.searchResults = [];
          $scope.searchParams.page = 1;
          $scope.searchParams.query = null;
          $scope.semanticType = null;
          $scope.termType = null;
          $scope.matchTerminology = null;
          $scope.language = null;
        };

        // Perform a search for the tree view
        $scope.setTreeView = function() {
          $scope.queryForTree = true;
          $scope.queryForList = false;
          if ($scope.searchParams.query) {
            $scope.searchParams.page = 1;
            $scope.findComponentsAsTree($scope.searchParams.query);
          }
        };

        // Perform a search for the list view
        $scope.setListView = function() {
          $scope.queryForList = true;
          $scope.queryForTree = false;
          if ($scope.searchParams.query) {
            $scope.searchParams.page = 1;
            $scope.findComponentsAsList($scope.searchParams.query);
          }
        };

        // Get a component and set the local component data model
        // e.g. this is called when a user clicks on a search result
        $scope.getComponent = function(type, terminologyId, terminology, version) {

          console.debug('getComponent', type, terminologyId, terminology, version);

          var wrapper = {
            type : type,
            terminologyId : terminologyId,
            terminology : terminology,
            version : version
          };

          contentService.getComponent(wrapper).then(function(response) {

            $scope.component = response;
            $scope.checkFavoriteStatus();
            $scope.setActiveRow(terminologyId);
            $scope.addComponentHistory();

          });
        };

        // Find components for a programmatic query
        $scope.findComponentsForQuery = function(queryStr) {
          $scope.searchParams.page = 1;
          $scope.searchParams.query = queryStr;
          $scope.findComponents(true);
        };

        $scope.performNewSearch = function(suppressWarnings) {
          $scope.searchParams.page = 1;
          $scope.searchResultsCollapsed = false;
          $scope.findComponents(true, suppressWarnings);
        };

        // Find concepts based on current search
        // - loadFirst indicates whether to auto-load result[0]
        $scope.findComponents = function(loadFirst, suppressWarnings) {
          $scope.searchOrBrowse = "SEARCH";
          if ($scope.queryForList) {
            $scope.findComponentsAsList(loadFirst, suppressWarnings);
          }
          if ($scope.queryForTree) {
            $scope.findComponentsAsTree(loadFirst, suppressWarnings);
          }
          $location.hash('top');
          $anchorScroll();

        };

        // Perform search and populate list view
        $scope.findComponentsAsList = function(loadFirst, suppressWarnings) {
          $scope.queryForTree = false;
          $scope.queryForList = true;

          // prerequisite checking
          var hasQuery = $scope.searchParams && $scope.searchParams.query
            && $scope.searchParams.query.length > 0;
          var hasExpr = $scope.searchParams && $scope.searchParams.advancedMode
            && $scope.searchParams.expression && $scope.searchParams.expression.value
            && $scope.searchParams.expression.value.length > 0;
          var hasNotes = $scope.searchParams && $scope.searchParams.advancedMode
            && $scope.searchParams.userNote;

          // ensure query/expression string has appropriate length
          if (!hasQuery && !hasExpr && !hasNotes) {
            if (!suppressWarnings) {
              alert("You must use at least one character to search"
                + ($scope.searchParams.advancedMode ? ($scope.metadata.terminology.descriptionLogicTerminology ? ", supply an expression,"
                  : "")
                  + " or search user notes"
                  : ""));

              // added to prevent weird bug causing page to scroll down a few
              // lines
              $location.hash('top');
            }
            return;
          }
          
          contentService.findComponentsAsList($scope.searchParams.query,
            $scope.metadata.terminology.organizingClassType,
            $scope.metadata.terminology.terminology, $scope.metadata.terminology.version,
            $scope.searchParams.page, $scope.searchParams).then(function(data) {
            $scope.searchResults = data;

            if (loadFirst && $scope.searchResults.results.length > 0) {
              // pass the search result (as wrapper)
              $scope.getComponentFromWrapper($scope.searchResults.results[0]);
            }
          });
        };

        // Perform search and populate tree view
        // - loadFirst is currently not used here
        $scope.findComponentsAsTree = function(loadFirst) {
          $scope.queryForTree = true;
          $scope.queryForList = false;

          // ensure query string has minimum length
          if (!$scope.searchParams.query || $scope.searchParams.query.length < 1) {
            alert("You must use at least one character to search");
            return;
          }

          contentService.findComponentsAsTree($scope.searchParams.query,
            $scope.metadata.terminology.organizingClassType,
            $scope.metadata.terminology.terminology, $scope.metadata.terminology.version,
            $scope.searchParams.page, $scope.searchParams).then(function(data) {

            // for ease and consistency of use of the ui tree
            // directive force the single tree into a ui-tree structure
            // with count variables
            $scope.searchResults.tree = [];
            $scope.searchResults.tree.push(data); // treeList
            // array of size 1
            $scope.searchResults.tree.totalCount = data.totalCount;
            $scope.searchResults.tree.count = data.count;
            // Load first functionality is not obvious here
            // so leave it alone for now.

          });
        };

        // set the top level component from a tree node
        // TODO Consider changing nodeTerminologyId to terminologyId, adding
        // type to allow wrapper universality
        $scope.getComponentFromTree = function(type, nodeScope) {
          console.debug('getComponentFromTree', type, nodeScope);
          var tree = nodeScope.$modelValue;
          $scope.getComponent(type, tree.nodeTerminologyId, tree.terminology, tree.version);
        };

        // helper function to get component from wrapper
        $scope.getComponentFromWrapper = function(wrapper) {
          console.debug('getComponentFromWrapper', wrapper);
          $scope.getComponent(wrapper.type, wrapper.terminologyId, wrapper.terminology,
            wrapper.version);
        };

        // Load hierarchy into tree view
        $scope.browseHierarchy = function() {
          $scope.searchOrBrowse = "BROWSE";
          $scope.queryForTree = true;
          $scope.queryForList = false;
          $scope.browsingHierarchy = true;
          $scope.searchParams.page = 1;
          $scope.searchParams.query = null;

          contentService.getTreeRoots($scope.metadata.terminology.organizingClassType,
            $scope.metadata.terminology.terminology, $scope.metadata.terminology.version,
            $scope.searchParams.page).then(function(data) {
            // for ease and consistency of use of the ui tree
            // directive
            // force the single tree into a ui-tree data
            // structure with count
            // variables
            $scope.queryForTree = true;
            $scope.searchResults.tree = [];
            $scope.searchResults.tree.push(data);
            // treeList array of size 1
            $scope.searchResults.tree.totalCount = data.totalCount;
            $scope.searchResults.tree.count = data.count;
          });
        };

        $scope.toggleAtomElement = function() {
          if ($scope.showAtomElement == null || $scope.showAtomElement == undefined) {
            $scope.showAtomElement = false;
          } else {
            $scope.showAtomElement = !$scope.showAtomElement;
          }

        };

        // //////////////////////////////////////////
        // Supporting search result trees
        // /////////////////////////////////////////

        // search result tree callbacks
        // NOTE Search Result Tree uses list search parameters
        $scope.srtCallbacks = {
          // set top level component from tree node
          getComponentFromTree : $scope.getComponentFromTree
        };

        // Function to toggle showing of extension info
        $scope.toggleExtension = function() {
          if ($scope.searchParams.showExtension == null
            || $scope.searchParams.showExtension == undefined) {
            $scope.searchParams.showExtension = false;
          } else {
            $scope.searchParams.showExtension = !$scope.searchParams.showExtension;
          }
        };

        // 
        // Misc helper functions
        // 

        // Helper function to select an item in the list view
        $scope.setActiveRow = function(terminologyId) {

          // set for search results
          if ($scope.searchResults && $scope.searchResults.results
            && $scope.searchResults.results.length > 0) {

            for (var i = 0; i < $scope.searchResults.results.length; i++) {
              if ($scope.searchResults.results[i].terminologyId === terminologyId) {
                $scope.searchResults.results[i].active = true;
              } else {
                $scope.searchResults.results[i].active = false;
              }
            }
          }

          // set for favorites
          if ($scope.favorites && $scope.favorites.results && $scope.favorites.results.length > 0) {

            for (var i = 0; i < $scope.favorites.results.length; i++) {
              if ($scope.favorites.results[i].terminologyId === terminologyId) {
                $scope.favorites.results[i].active = true;
              } else {
                $scope.favorites.results[i].active = false;
              }
            }
          }

        };

        //
        // METADATA related functions
        //

        // Find a terminology version
        $scope.getTerminologyVersion = function(terminology) {
          for (var i = 0; i < $scope.metadata.terminologies.length; i++) {
            if (terminology === $scope.metadata.terminologies[i].terminology) {
              return $scope.metadata.terminologies[i].version;
            }
          }
        };
        // Function to filter viewable terminologies for picklist
        $scope.getViewableTerminologies = function() {
          var viewableTerminologies = new Array();
          if (!$scope.metadata.terminologies) {
            return null;
          }
          for (var i = 0; i < $scope.metadata.terminologies.length; i++) {
            // exclude MTH and SRC
            if ($scope.metadata.terminologies[i].terminology != 'MTH'
              && $scope.metadata.terminologies[i].terminology != 'SRC')
              viewableTerminologies.push($scope.metadata.terminologies[i]);
          }
          return viewableTerminologies;
        };

        // 
        // HISTORY related functions
        //

        // Local history variables for the display.
        $scope.history = contentService.getHistory();
        $scope.historyPage = {};

        function setHistoryPage() {

          console.debug('setHistoryPage: ', $scope.history);

          // convenience variables
          var hps = parseInt($scope.pageSizes.general / 2);
          var ct = $scope.history.components.length;
          var index = $scope.history.index;

          // get the from and to indices
          var fromIndex = Math.max(index - (2 * hps - Math.min(ct - index, hps)), 0);
          var toIndex = Math.min(fromIndex + 2 * hps, ct);

          // slice the components
          var components = $scope.history.components.slice(fromIndex, toIndex);

          // assign indices for retrieval convenience
          for (var i = 0; i < components.length; i++) {
            components[i].index = fromIndex + i;
          }

          // set the scope variable
          $scope.historyPage = {
            fromIndex : fromIndex,
            toIndex : toIndex,
            components : components
          };
        }

        // Retrieve a component from the history list
        $scope.getComponentFromHistory = function(index) {

          // if currently viewed do nothing
          if (index === $scope.history.index) {
            return;
          }

          contentService.getComponentFromHistory(index).then(function(data) {
            $scope.component = data;
            $scope.checkFavoriteStatus();
            setHistoryPage();
          });
        };

        // Get a string representation fo the component
        $scope.getComponentHistoryStr = function(component) {
          if (!component)
            return null;

          return component.terminology + "/" + component.terminologyId + " " + component.type
            + ": " + component.name;
        };

        // Function to set the local history for drop down list based on
        // an index For cases where history > page size, returns array
        // [index - pageSize / 2 + 1 : index + pageSize]
        $scope.addComponentHistory = function(index) {
          contentService.addComponentToHistory($scope.component.terminologyId,
            $scope.component.terminology, $scope.component.version, $scope.component.type,
            $scope.component.name);
          setHistoryPage();

        };

        // Pop out content window
        $scope.popout = function() {
          var currentUrl = window.location.href;
          var baseUrl = currentUrl.substring(0, currentUrl.lastIndexOf('/'));
          // TODO; don't hardcode this - maybe "simple" should be a parameter
          var newUrl = baseUrl + '/content/simple/' + $scope.component.type + '/'
            + $scope.component.terminology + '/' + $scope.component.version + '/'
            + $scope.component.terminologyId;
          var myWindow = window.open(newUrl, $scope.component.terminology + '/'
            + $scope.component.version + ', ' + $scope.component.terminologyId + ', '
            + $scope.component.name);
          myWindow.focus();
        };

        //
        // Expression handling
        //

        // Set expression
        $scope.setExpression = function() {
          // ensure all fields set to wildcard if not set
          for ( var key in $scope.searchParams.expression.fields) {
            if ($scope.searchParams.expression.fields.hasOwnProperty(key)) {
              if (!$scope.searchParams.expression.fields[key]) {
                $scope.searchParams.expression.fields[key] = '*';
              }
            }
          }

          // call the expression's pattern generator
          $scope.searchParams.expression.compute();

          // replace wildcards with blank values again
          // TODO Very clunky, obviously
          for ( var key in $scope.searchParams.expression.fields) {
            if ($scope.searchParams.expression.fields.hasOwnProperty(key)) {
              if ($scope.searchParams.expression.fields[key] === '*') {
                $scope.searchParams.expression.fields[key] = '';
              }
            }
          }

        };

        // clears the fields, computed value and resets selected expression
        $scope.clearExpression = function() {
          for ( var key in $scope.searchParams.expression.fields) {
            $scope.searchParams.expression.fields[key] = null;
          }
          $scope.searchParams.expression.value = null;
          $scope.searchParams.expression = $scope.expressions[0];
        };

        // get the defined expressions and set to the first option
        $scope.configureExpressions = function() {
          $scope.expressions = contentService.getExpressions();
          $scope.searchParams.expression = $scope.expressions[0];
        };

        $scope.selectComponent = function(key) {

          var modalScope = $rootScope.$new();

          var modalInstance = $uibModal.open({
            animation : $scope.animationsEnabled,
            templateUrl : 'app/util/select-component-modal/selectComponentModal.html',
            controller : 'selectComponentModalCtrl',
            scope : $rootScope,
            size : 'lg',
            resolve : {
              metadata : function() {
                return $scope.metadata;
              }
            }
          });

          modalInstance.result.then(function(component) {
            $scope.searchParams.expression.fields[key] = component.terminologyId + ' | '
              + component.name + ' |';
            $scope.setExpression();
          }, function() {
            // do nothing
          });
        };

        // Open notes modal, from either wrapper or component
        $scope.viewNotes = function(wrapper) {

          var modalInstance = $uibModal.open({
            animation : $scope.animationsEnabled,
            templateUrl : 'app/util/component-note-modal/componentNoteModal.html',
            controller : 'componentNoteModalCtrl',
            scope : $rootScope,
            size : 'lg',
            resolve : {
              component : function() {
                return $scope.component;

              }
            }
          });

          // on close or cancel, re-retrieve the concept for updated notes
          modalInstance.result.then(function() {
            // re-retrieve the concept
            $scope.getComponentFromWrapper($scope.component);
          }, function() {
            // re-retrieve the concept
            $scope.getComponentFromWrapper($scope.component);
          });
        };

        //
        // Favorites
        //

        // Check favorite status
        $scope.checkFavoriteStatus = function() {
          $scope.isFavorite = $scope.component ? securityService.isUserFavorite(
            $scope.component.type, $scope.component.terminology, $scope.component.version,
            $scope.component.terminologyId) : false;
        };

        // Toggle favorite
        $scope.toggleFavorite = function(type, terminology, version, terminologyId, name) {
          if (securityService.isUserFavorite(type, terminology, version, terminologyId)) {
            securityService.removeUserFavorite(type, terminology, version, terminologyId, name)
              .then(function() {
                $scope.isFavorite = false;
                websocketService.fireFavoriteChange();
              });
          } else {
            securityService.addUserFavorite(type, terminology, version, terminologyId, name).then(
              function() {
                $scope.isFavorite = true;
                websocketService.fireFavoriteChange();
              });
          }
        };

        //
        // Callback Function Objects
        //
        $scope.configureCallbacks = function() {
          console.debug('Initializing content controller callback objects');

          // declare the callbacks objects
          $scope.componentRetrievalCallbacks = {};
          $scope.componentReportCallbacks = {};
          $scope.favoritesCallbacks = {};

          //
          // Local scope functions pertaining to component retrieval
          //
          $scope.componentRetrievalCallbacks = {
            getComponent : $scope.getComponent,
            getComponentFromTree : $scope.getComponentFromTree,
            getComponentFromWrapper : $scope.getComponentFromWrapper,
            findComponentsForQuery : $scope.findComponentsForQuery
          };
          console.debug('  component retrieval callbacks', $scope.componentRetrievalCallbacks);

          //
          // Component report callbacks
          //

          // pass metadata callbacks for tooltip and general display
          utilService.extendCallbacks($scope.componentReportCallbacks, metadataService
            .getCallbacks());

          // add content callbacks for special content retrieval (relationships,
          // mappings, etc.)
          utilService.extendCallbacks($scope.componentReportCallbacks, contentService
            .getCallbacks());

          // if in simple mode
          if ($routeParams.mode === 'simple') {
            // do nothing
          } else {
            // add content callbacks for top-level component retrieval
            utilService.extendCallbacks($scope.componentReportCallbacks,
              $scope.componentRetrievalCallbacks);
          }
          console.debug('  Component report callbacks', $scope.componentReportCallbacks);

          //
          // Favorites Callbacks
          // 
          $scope.favoritesCallbacks = {
            checkFavoriteStatus : $scope.checkFavoriteStatus
          };
          // add content callbacks for top-level component retrieval
          utilService
            .extendCallbacks($scope.favoritesCallbacks, $scope.componentRetrievalCallbacks);
          console.debug('  Favorites callbacks', $scope.favoritesCallbacks);

        };

        //
        // Initialize
        //

        $scope.initialize = function() {

          $scope.configureTab();
          $scope.configureExpressions();
          $scope.configureCallbacks();

          //
          // Check for values preserved in content service (after route changes)
          //
          if (contentService.getLastSearchParams()) {
            $scope.searchParams = contentService.getLastSearchParams();
            $scope.findComponents(false, true);
          }
          if (contentService.getLastComponent()) {
            $scope.component = contentService.getLastComponent();
            $scope.checkFavoriteStatus();
          }

          // Load all terminologies upon controller load (unless already
          // loaded)
          if (!$scope.metadata.terminologies || !$scope.metadata.terminology) {
            metadataService.initTerminologies().then(
              // success
              function(data) {

                // if route parameters are specified, set the terminology and
                // retrieve
                // the specified concept
                if ($routeParams.terminology && $routeParams.version) {

                  var termToSet = null;
                  for (var i = 0; i < $scope.metadata.terminologies.length; i++) {
                    var terminology = $scope.metadata.terminologies[i];
                    // Determine whether to set as default
                    if (terminology.terminology === $routeParams.terminology
                      && terminology.version === $routeParams.version) {
                      termToSet = terminology;
                      break;
                    }
                  }

                  if (!termToSet) {
                    utilService.setError('Terminology specified in URL not found');
                  } else {

                    // set the terminology
                    $scope.setTerminology(termToSet).then(
                      function() {

                        // get the component
                        $scope.getComponent($routeParams.type, $routeParams.terminologyId,
                          $routeParams.terminology, $routeParams.version);
                      });
                  }
                }

                // otherwise, specify the default terminology
                else {

                  var found = false;
                  if ($scope.user.userPreferences && $scope.user.userPreferences.lastTerminology) {
                    for (var i = 0; i < $scope.metadata.terminologies.length; i++) {
                      var terminology = $scope.metadata.terminologies[i];
                      // set from user prefs
                      if (terminology.terminology === $scope.user.userPreferences.lastTerminology) {
                        $scope.setTerminology(terminology);
                        found = true;
                        break;
                      }
                    }
                  }

                  // otherwise look for metathesaurus
                  if (!found) {
                    for (var i = 0; i < $scope.metadata.terminologies.length; i++) {
                      var terminology = $scope.metadata.terminologies[i];
                      // Determine whether to set as default
                      if (terminology.metathesaurus) {
                        $scope.setTerminology(terminology);
                        found = true;
                        break;
                      }
                    }
                  }

                  // If nothing set, pick the first one
                  if (!found) {
                    if (!$scope.metadata.terminologies) {
                      window.alert('No terminologies found, database may not be properly loaded.');
                    } else {
                      $scope.setTerminology($scope.metadata.terminologies[0]);
                    }
                  }
                }
              });
          }
        };

        //
        // Initialization: Check
        // (1) that application is configured, and
        // (2) that the license has been accepted (if required)
        //
        configureService.isConfigured().then(function(isConfigured) {
          if (!isConfigured) {
            $location.path('/configure');
          } else {
            securityService.checkLicense().then(function() {
              console.debug('License valid, initializing');
              $scope.initialize();
            }, function() {
              console.debug('Invalid license');
              utilService.setError('You must accept the license before viewing that content');
              $location.path('/license');
            });
          }
        });

      }

    ]);
