// Semantic types controller

tsApp
  .controller(
    'RelationshipsCtrl',
    [
      '$scope',
      '$window',
      'utilService',
      'contentService',
      'tabService',
      'securityService',
      'utilService',
      'metaEditingService',
      '$uibModal',
      function($scope, $window, utilService, contentService, tabService, securityService,
        utilService, metaEditingService, $uibModal) {

        console.debug("configure RelationshipsCtrl");

        // remove tabs, header and footer
        tabService.setShowing(false);
        utilService.setHeaderFooterShowing(false);

        // preserve parent scope reference
        $scope.parentWindowScope = window.opener.$windowScope;
        window.$windowScope = $scope;
        $scope.selected = $scope.parentWindowScope.selected;
        $scope.lists = $scope.parentWindowScope.lists;
        $scope.user = $scope.parentWindowScope.user;
        $scope.selected.relationship = null;

        // Paging variables
        $scope.paging = {};
        $scope.paging = utilService.getPaging();
        $scope.paging.sortField = 'id';
        $scope.paging.pageSize = 10;
        $scope.paging.filterFields = {};
        $scope.paging.filterFields.toName = 1;
        $scope.paging.filterFields.fromName = 1;
        $scope.paging.filterFields.toTerminologyId = 1;
        $scope.paging.filterFields.fromTerminologyId = 1;
        $scope.paging.filterFields.relationshipType = 1;
        $scope.paging.filterFields.additionalRelationshipType = 1;
        $scope.paging.filterFields.terminology = 1;
        $scope.paging.filterFields.lastModifiedBy = 1;
        $scope.paging.sortAscending = false;
        $scope.paging.callbacks = {
          getPagedList : getPagedRelationships
        };

        $scope.$watch('selected.component', function() {
          console.debug('in watch');
          $scope.selected.relationship = null;
          $scope.getPagedRelationships();
        });

        // add relationship
        $scope.addRelationshipToConcept = function(relationship) {
          metaEditingService.addRelationship($scope.selected.project.id, null,
            $scope.selected.component, relationship);
        }

        // remove relationship
        $scope.removeRelationshipFromConcept = function(relationship) {
          metaEditingService.removeRelationship($scope.selected.project.id, null,
            $scope.selected.component, relationship.id, true);
        }

        // Get paged relationships
        $scope.getPagedRelationships = function() {
          getPagedRelationships();
        }
        function getPagedRelationships() {
          var paging = $scope.paging;
          var pfs = {
            startIndex : (paging.page - 1) * paging.pageSize,
            maxResults : paging.pageSize,
            sortField : paging.sortField,
            ascending : paging.sortAscending,
            queryRestriction : paging.filter
          };

          contentService.findRelationshipsForQuery({
            terminology : $scope.selected.project.terminology,
            version : $scope.selected.project.version,
            terminologyId : $scope.selected.component.terminologyId,
            type : $scope.selected.component.type
          }, null, pfs).then(
          // Success
          function(data) {
            $scope.pagedRelationships = data.relationships;
            $scope.pagedRelationships.totalCount = data.totalCount

          });
        }

        $scope.transferConceptToEditor = function() {
          $scope.parentWindowScope.transferConceptToEditor($scope.selected.relationship.toId);
        }

        // refresh
        $scope.refresh = function() {
          $scope.$apply();
        }

        // notify edit controller when semantic type window closes
        $window.onbeforeunload = function(evt) {
          $scope.parentWindowScope.removeWindow('relationship');
        }

        // Table sorting mechanism
        $scope.setSortField = function(table, field, object) {
          utilService.setSortField(table, field, $scope.paging);
          $scope.getPagedRelationships();
        };

        // Return up or down sort chars if sorted
        $scope.getSortIndicator = function(table, field) {
          return utilService.getSortIndicator(table, field, $scope.paging);
        };

        // indicates the style for an relationship
        $scope.getRelationshipClass = function(relationship) {

          // NEEDS_REVIEW (red)
          if (relationship.workflowStatus == 'NEEDS_REVIEW')
            return 'NEEDS_REVIEW';

          // UNRELEASABLE (green)
          if (!relationship.publishable)
            return 'UNRELEASABLE';

          // RXNORM (orange)
          if (relationship.terminology == 'RXNORM') {
            return 'RXNORM';
          }

          // OBSOLETE (purple)
          if (relationship.obsolete)
            return 'OBSOLETE';

          // REVIEWED READY_FOR_PUBLICATION (black)
          return 'READY_FOR_PUBLICATION';

        }

        // selects an relationship
        $scope.selectRelationship = function(event, relationship) {
          $scope.selected.relationship = relationship;
        };

        // indicates if a particular row is selected
        $scope.isRowSelected = function(relationship) {
          return $scope.selected.relationship && $scope.selected.relationship.id == relationship.id;
        }

        //
        // Modals
        //

        // Merge modal
        $scope.openMergeModal = function() {

          var modalInstance = $uibModal.open({
            templateUrl : 'app/page/edit/mergeMoveSplit.html',
            controller : 'MergeMoveSplitModalCtrl',
            backdrop : 'static',
            resolve : {
              selected : function() {
                return $scope.selected;
              },
              lists : function() {
                return $scope.lists;
              },
              action : function() {
                return 'Merge';
              },
              user : function() {
                return $scope.user;
              }
            }
          });

          modalInstance.result.then(
          // Success
          function(data) {
            $scope.parentWindowScope.getRecords(false);
            $scope.parentWindowScope.getConcepts($scope.selected.record);
            $scope.getPagedRelationships();
          });
        };

        // Insert modal
        $scope.openInsertModal = function() {
          if (!$scope.selected.relationship && $scope.lists.concepts.length < 2) {
            window
              .alert('There is only one concept on the concept list.  Select a \'to\' concept for the relationship.');
            return;
          }
          var modalInstance = $uibModal.open({
            templateUrl : 'app/page/edit/relationships/editRelationship.html',
            controller : 'EditRelationshipModalCtrl',
            backdrop : 'static',
            resolve : {
              selected : function() {
                return $scope.selected;
              },
              lists : function() {
                return $scope.lists;
              },
              user : function() {
                return $scope.user;
              },
              action : function() {
                return 'Add';
              }
            }
          });

          modalInstance.result.then(
          // Success
          function(data) {
            $scope.getPagedRelationships();
          });
        };

        //
        // Initialize - DO NOT PUT ANYTHING AFTER THIS SECTION
        //
        $scope.initialize = function() {
          $scope.getPagedRelationships();
        }

        // Call initialize
        $scope.initialize();

      } ]);