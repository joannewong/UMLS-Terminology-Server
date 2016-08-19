// Worklist table
// e.g. <div worklist-table value='Worklist' />
tsApp
  .directive(
    'worklistsTable',
    [
      '$uibModal',
      '$window',
      '$sce',
      '$interval',
      'utilService',
      'securityService',
      'projectService',
      'workflowService',
      function($uibModal, $window, $sce, $interval, utilService, securityService, projectService,
        workflowService) {
        console.debug('configure worklistTable directive');
        return {
          restrict : 'A',
          scope : {
            // Legal 'value' settings include:
            // Worklist, Checklist
            type : '@',
            selected : '=',
            lists : '=',
            user : '='
          },
          templateUrl : 'app/component/worklists/worklistsTable.html',
          controller : [
            '$scope',
            function($scope) {

              // Reset some scope settings
              $scope.selected.worklist = null;
              $scope.selected.record = null;
              $scope.selected.concept = null;

              $scope.lists.records = [];
              $scope.lists.users = [];

              // This structure reused so don't conflate
              $scope.worklists = [];

              // Paging variables
              $scope.paging = {};
              $scope.paging['worklists'] = utilService.getPaging();
              $scope.paging['worklists'].sortField = 'lastModified';
              $scope.paging['worklists'].callback = {
                getPagedList : getWorklists
              };
              $scope.paging['records'] = utilService.getPaging();
              $scope.paging['records'].sortField = 'clusterId';
              $scope.paging['records'].callback = {
                getPagedList : getRecords
              };

              // Project Changed Handler
              $scope.$watch('selected', function() {
                if ($scope.selected.project) {
                  // Set project, refresh worklist list
                  $scope.setProject($scope.selected.project);
                }
              }, true);

              // Compose a string of all editors for display
              $scope.joinEditors = function(worklist) {
                if (worklist.reviewers) {
                  return worklist.reviewers.join(' ');
                } else if (worklist.authors) {
                  return worklist.authors.join(' ');
                }
                return '';
              };

              // Set $scope.selected.project and reload
              $scope.setProject = function(project) {
                $scope.project = project;
                $scope.getWorklists();
                projectService.findAssignedUsersForProject($scope.project.id, null, null).then(
                  function(data) {
                    $scope.lists.users = data.users;
                    $scope.lists.users.totalCount = data.totalCount;
                  });
              };

              // Get $scope.worklists
              $scope.getWorklists = function() {
                getWorklists();
              }
              function getWorklists() {
                var paging = $scope.paging['worklists'];
                var pfs = {
                  startIndex : (paging.page - 1) * paging.pageSize,
                  maxResults : paging.pageSize,
                  sortField : paging.sortField,
                  ascending : paging.sortAscending,
                  queryRestriction : paging.filter
                };

                if ($scope.type == 'Worklist') {
                  workflowService.findWorklists($scope.selected.project.id, $scope.query, pfs)
                    .then(function(data) {
                      $scope.worklists = data.worklists;
                      $scope.worklists.totalCount = data.totalCount;
                    });
                }
                if ($scope.type == 'Checklist') {
                  workflowService.findChecklists($scope.selected.project.id, $scope.query, pfs)
                    .then(function(data) {
                      $scope.worklists = data.checklists;
                      $scope.worklists.totalCount = data.totalCount;
                    });
                }
              }
              ;

              // Get $scope.records
              $scope.getRecords = function() {
                getRecords();
              }
              function getRecords() {

                var paging = $scope.paging['records'];
                var pfs = {
                  startIndex : (paging.page - 1) * paging.pageSize,
                  maxResults : paging.pageSize,
                  sortField : paging.sortField,
                  ascending : paging.sortAscending,
                  queryRestriction : paging.filter
                };

                if (paging.typeFilter) {
                  var value = paging.typeFilter;

                  // Handle status
                  if (value == 'N') {
                    pfs.queryRestriction += (pfs.queryRestriction ? ' AND ' : '')
                      + ' workflowStatus:N*';
                  } else if (value == 'R') {
                    pfs.queryRestriction += (pfs.queryRestriction ? ' AND ' : '')
                      + ' workflowStatus:R*';
                  }

                }

                if ($scope.type == 'Worklist') {

                  workflowService.findTrackingRecordsForWorklist($scope.selected.project.id,
                    $scope.selected.worklist.id, pfs).then(
                  // Success
                  function(data) {
                    $scope.lists.records = data.records;
                    $scope.lists.records.totalCount = data.totalCount;
                  });
                } else if ($scope.type == 'Checklist') {
                  workflowService.findTrackingRecordsForChecklist($scope.selected.project.id,
                    $scope.selected.worklist.id, pfs).then(
                  // Success
                  function(data) {
                    $scope.lists.records = data.records;
                    $scope.lists.records.totalCount = data.totalCount;
                  });
                }

              }
              ;

              // Convert time to a string
              $scope.toTime = function(editingTime) {
                return utilService.toTime(editingTime);
              };

              // Convert date to a string
              $scope.toDate = function(lastModified) {
                return utilService.toDate(lastModified);
              };

              // Table sorting mechanism
              $scope.setSortField = function(table, field, object) {
                utilService.setSortField(table, field, $scope.paging);

                // retrieve the correct table
                if (table === 'worklists') {
                  $scope.getWorklists();
                }
                if (table === 'records') {
                  $scope.getRecords();
                }
              };

              // Return up or down sort chars if sorted
              $scope.getSortIndicator = function(table, field) {
                return utilService.getSortIndicator(table, field, $scope.paging);
              };

              // Selects a worklist (setting $scope.selected.worklist).
              // Looks up current release info and records.
              $scope.selectWorklist = function(worklist) {
                $scope.selected.worklist = worklist;
                // clear selected concept
                $scope.selected.concept = null;
                if ($scope.type == 'Worklist') {
                  $scope.parseStateHistory(worklist);
                }
                $scope.getRecords(worklist);
              };

              // parse workflow state history
              $scope.parseStateHistory = function(worklist) {
                $scope.stateHistory = [];
                var states = Object.keys(worklist.workflowStateHistory);
                for (var i = 0; i < states.length; i++) {
                  var state = {
                    name : states[i],
                    timestamp : worklist.workflowStateHistory[states[i]]
                  }
                  $scope.stateHistory.push(state);
                }
              }

              // Unassign worklist
              $scope.unassignWorklist = function(worklist) {
                workflowService.performWorkflowAction($scope.selected.project.id, worklist.id,
                  $scope.joinEditors(worklist).trim(),
                  $scope.selected.project.userRoleMap[$scope.user.userName], 'UNASSIGN').then(
                // Success
                function(data) {
                  $scope.getWorklists();
                });
              };

              // Remove a worklist
              $scope.removeWorklist = function(worklist) {
                $scope.removeWorklistHelper($scope.selected.project.id, worklist);
              };

              // Helper for removing a worklist/checklist
              $scope.removeWorklistHelper = function(projectId, worklist) {

                /*
                 * workflowService.findWorklistMembersForQuery(worklist.id, '', {
                 * startIndex : 0, maxResults : 1 }).then( function(data) { if
                 * (data.records.length == 1) { if (!$window .confirm('The
                 * worklist has records, are you sure you want to proceed.')) {
                 * return; } }
                 */
                if ($scope.type == 'Worklist') {
                  workflowService.removeWorklist(projectId, worklist.id).then(function() {
                    $scope.selected.worklist = null;
                    $scope.getWorklists();
                    workflowService.fireWorklistChanged(worklist);
                    workflowService.fireWorkflowBinsChanged(worklist);
                  });
                } else {
                  workflowService.removeChecklist(projectId, worklist.id).then(function() {
                    $scope.selected.worklist = null;
                    $scope.getWorklists();
                    workflowService.fireWorklistChanged(worklist);
                    workflowService.fireWorkflowBinsChanged(worklist);
                  });
                }
                // });
              };

              // Unassign worklist from user
              $scope.unassign = function(worklist, userName) {
                $scope.performWorkflowAction(worklist, 'UNASSIGN', userName);
              };

              // handle workflow advancement
              $scope.handleWorkflow = function(worklist) {
                if ($scope.type == 'ASSIGNED'
                  && worklist
                  && (worklist.workflowStatus == 'NEW' || worklist.workflowStatus == 'READY_FOR_PUBLICATION')) {
                  $scope.performWorkflowAction(worklist, 'SAVE', $scope.user.userName);
                } else {
                  $scope.getWorklists();
                  workflowService.fireWorklistChanged(worklist);
                }
              };

              // Performs a workflow action
              $scope.performWorkflowAction = function(worklist, action, userName) {

                workflowService.performWorkflowAction($scope.selected.project.id, worklist.id,
                  userName, $scope.selected.projects.role, action).then(function(data) {
                  $scope.getWorklists();
                  workflowService.fireWorklistChanged(data);
                });
              };

              // Get the most recent note for display
              $scope.getLatestNote = function(worklist) {
                if (worklist && worklist.notes && worklist.notes.length > 0) {
                  return $sce.trustAsHtml(worklist.notes.sort(utilService.sort_by('lastModified',
                    -1))[0].note);
                }
                return $sce.trustAsHtml('');
              };

              //
              // MODALS
              //

              // Notes modal
              $scope.openNotesModal = function(lobject) {
                console.debug('openNotesModal ', lobject);

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/page/workflow/notes.html',
                  controller : NotesModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    object : function() {
                      return lobject;
                    },
                    value : function() {
                      return $scope.type;
                    },
                    project : function() {
                      return $scope.selected.project;
                    },
                    tinymceOptions : function() {
                      return utilService.tinymceOptions;
                    }
                  }
                });

                modalInstance.result.then(
                // Success
                function(data) {
                  $scope.handleWorkflow(data);
                });

              };

              // Assign worklist modal
              $scope.openAssignWorklistModal = function(lworklist, laction, lrole) {
                console.debug('openAssignWorklistModal ', lworklist, laction);

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/page/workflow/assignWorklist.html',
                  controller : AssignWorklistModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    worklist : function() {
                      return lworklist;
                    },
                    action : function() {
                      return laction;
                    },
                    currentUser : function() {
                      return $scope.user;
                    },
                    project : function() {
                      return $scope.selected.project;
                    }
                  }

                });

                modalInstance.result.then(
                // Success
                function(data) {
                  $scope.getWorklists();
                  workflowService.fireWorklistChanged(data);
                });
              };

              // end

            } ]
        };
      } ]);