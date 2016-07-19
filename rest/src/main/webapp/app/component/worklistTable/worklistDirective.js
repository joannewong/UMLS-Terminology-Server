// Worklist Table directive
// e.g. <div worklist-table value='PUBLISHED' />
tsApp
  .directive(
    'worklistTable',
    [
      '$uibModal',
      '$window',
      '$sce',
      '$interval',
      'utilService',
      'securityService',
      'projectService',
      'workflowService',
      'reportService',
      'validationService',
      function($uibModal, $window, $sce, $interval, utilService, securityService, projectService,
        workflowService, reportService, validationService) {
        console.debug('configure worklistTable directive');
        return {
          restrict : 'A',
          scope : {
            // Legal 'value' settings include
            // For directory tab: PUBLISHED, BETA
            // For worklist tab: AVAILABLE, ASSIGNED
            // RELEASE
            value : '@',
            projects : '=',
            metadata : '=',
            stats : '=',
            project : '='
          },
          templateUrl : 'app/component/worklistTable/worklistTable.html',
          controller : [
            '$scope',
            function($scope) {

              // Variables
              $scope.user = securityService.getUser();
              $scope.userProjectsInfo = projectService.getUserProjectsInfo();
              $scope.selected = {
                worklist : null,
                record : null,
                concept : null,
                terminology : null,
                version : null
              };
              $scope.worklists = null;
              //$scope.project = null;
              $scope.filters = [];
                           

              // Page metadata
              $scope.recordTypes = [ 'N', 'R' ];

              // Used for project admin to know what users are assigned to
              // something.
              $scope.worklistAuthorsMap = {};
              $scope.worklistReviewersMap = {};

              // Paging variables
              $scope.visibleSize = 4;
              $scope.pageSize = 10;
              $scope.paging = {};
              $scope.paging['worklist'] = {
                page : 1,
                filter : '',
                sortField : 'lastModified',
                ascending : null
              };
              $scope.paging['record'] = {
                page : 1,
                filter : '',
                typeFilter : '',
                sortField : 'clusterId',
                ascending : true
              };


              // Worklist Changed handler
              $scope.$on('workflow:worklistChanged', function(event, data) {
                console.debug('on workflow:worklistChanged', data);
                $scope.getWorklists();
              });

              // Project Changed Handler
              $scope.$on('worklist:projectChanged', function(event, data) {
                console.debug('on worklist:projectChanged', data);
                // Set project, refresh worklist list
                $scope.setProject(data);
              });

              // link to error handling
              function handleError(errors, error) {
                utilService.handleDialogError(errors, error);
              }

              // Set $scope.project and reload
              // $scope.worklists
              $scope.setProject = function(project) {
                $scope.project = project;
                $scope.getWorklists();
              };

              // Get $scope.worklists
              $scope.getWorklists = function() {
                var pfs = {
                  startIndex : ($scope.paging['worklist'].page - 1) * $scope.pageSize,
                  maxResults : $scope.pageSize,
                  sortField : $scope.paging['worklist'].sortField,
                  ascending : $scope.paging['worklist'].ascending == null ? true
                    : $scope.paging['worklist'].ascending,
                  queryRestriction : null
                };

                if ($scope.value == 'Worklist') {
                  pfs.queryRestriction = $scope.paging['worklist'].filter;
                  workflowService.findWorklists($scope.project.id,
                    $scope.query, pfs).then(function(data) {
                    $scope.worklists = data.worklists;
                    $scope.worklists.totalCount = data.totalCount;
                  });
                }
                if ($scope.value == 'Checklist') {
                  pfs.queryRestriction = $scope.paging['worklist'].filter;
                  workflowService.findChecklists($scope.project.id,
                    $scope.query, pfs).then(function(data) {
                    $scope.worklists = data.checklists;
                    $scope.worklists.totalCount = data.totalCount;
                  });
                }
              };


              // Get $scope.records
              $scope.getRecords = function(worklist) {

                var pfs = {
                  startIndex : ($scope.paging['record'].page - 1) * $scope.pageSize,
                  maxResults : $scope.pageSize,
                  sortField : $scope.paging['record'].sortField,
                  ascending : $scope.paging['record'].ascending == null ? false
                    : $scope.paging['record'].ascending,
                  queryRestriction : $scope.paging['record'].filter != undefined && 
                  $scope.paging['record'].filter != "" ? $scope.paging['record'].filter
                    : null
                };

                if ($scope.paging['record'].typeFilter) {
                  var value = $scope.paging['record'].typeFilter;

                  // Handle inactive
                  if (value == 'N') {
                    if (pfs.queryRestriction != null)
                      pfs.queryRestriction += ' AND workflowStatus:NEEDS_REVIEW';
                    else
                      pfs.queryRestriction = 'workflowStatus:NEEDS_REVIEW';
                  } else if (value == 'R') {
                    if (pfs.queryRestriction != null)
                      pfs.queryRestriction += ' AND workflowStatus:READY_FOR_PUBLICATION';
                    else
                      pfs.queryRestriction = 'workflowStatus:READY_FOR_PUBLICATION';
                  }

                }

                if ($scope.value == 'Worklist') {

                  workflowService.findTrackingRecordsForWorklist(worklist.projectId, worklist.id,
                    pfs).then(
                  // Success
                  function(data) {
                    worklist.records = data.worklists;
                    worklist.records.totalCount = data.totalCount;
                  });
                } else if ($scope.value == 'Checklist') {
                  workflowService.findTrackingRecordsForChecklist(worklist.projectId, worklist.id,
                    pfs).then(
                  // Success
                  function(data) {
                    worklist.records = data.worklists;
                    worklist.records.totalCount = data.totalCount;
                  });
                }

              };


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

                // handle 'ASSIGNED' vs 'AVAILABLE' fields
                // worklistTable.html expresses the fields in terms of available
                var lfield = field;
                if (table == 'worklist' && ($scope.value == 'ASSIGNED')) {
                  if (field == 'terminologyId') {
                    lfield = 'worklistId';
                  } else if (field == 'lastModified') {
                    lfield = 'lastModified';
                  } else if (field == 'workflowStatus') {
                    lfield = 'workflowStatus';
                  } else {
                    // uppercase and prepend worklist in all other cases
                    lfield = 'worklist' + field.charAt(0).toUpperCase() + field.slice(1);
                  }
                }

                utilService.setSortField(table, lfield, $scope.paging);
                // retrieve the correct table
                if (table === 'worklist') {
                  $scope.getWorklists();
                }
                if (table === 'record') {
                  $scope.getRecords(object);
                }
              };

              // Return up or down sort chars if sorted
              $scope.getSortIndicator = function(table, field) {
                var lfield = field;
                if (table == 'worklist' && ($scope.value == 'ASSIGNED')) {
                  if (field == 'terminologyId') {
                    lfield = 'worklistId';
                  } else if (field == 'lastModified') {
                    lfield = 'lastModified';
                  } else if (field == 'workflowStatus') {
                    lfield = 'workflowStatus';
                  } else {
                    // uppercase and prepend worklist in all other cases
                    lfield = 'worklist' + field.charAt(0).toUpperCase() + field.slice(1);
                  }
                }
                return utilService.getSortIndicator(table, lfield, $scope.paging);
              };

              // Selects a worklist (setting $scope.selected.worklist).
              // Looks up current release info and records.
              $scope.selectWorklist = function(worklist) {
                $scope.selected.worklist = worklist;
                $scope.selected.terminology = worklist.terminology;
                $scope.selected.version = worklist.version;
                $scope.selected.concept = null;
                $scope.getRecords(worklist);
              };

              // Selects a concept (setting $scope.selected.concept)
              $scope.selectConcept = function(concept) {
                // Set the concept for display
                $scope.selected.concept = {
                  terminologyId : concept.terminologyId,
                  terminology : concept.terminology,
                  version : concept.version,
                  id : concept.id
                };
                reportService.getConceptReport($scope.project.id, $scope.selected.concept.id).then(
                // Success
                function(data) {
                  $scope.selected.concept.report = data;
                });
              };


              // Remove a worklist
              $scope.removeWorklist = function(worklist) {
               /* workflowService.findAllAssignedWorklists($scope.project.id, {
                  startIndex : 0,
                  maxResults : 1,
                  queryRestriction : 'worklistId:' + worklist.id
                }).then(
                  // Success
                  function(data) {
                    if (data.records.length > 0
                      && !$window
                        .confirm('The worklist is assigned, are you sure you want to proceed?')) {
                      return;
                    }*/
                    $scope.removeWorklistHelper($scope.project.id, worklist);
                  //});
              };

              // Helper for removing a refest
              $scope.removeWorklistHelper = function(projectId, worklist) {

                /*workflowService.findWorklistMembersForQuery(worklist.id, '', {
                  startIndex : 0,
                  maxResults : 1
                }).then(
                  function(data) {
                    if (data.records.length == 1) {
                      if (!$window
                        .confirm('The worklist has records, are you sure you want to proceed.')) {
                        return;
                      }
                    }*/
                    workflowService.removeWorklist(projectId, worklist.id).then(function() {
                      $scope.selected.worklist = null;
                      workflowService.fireWorklistChanged(worklist);
                    });
                  //});
              };



              // Unassign worklist from user
              $scope.unassign = function(worklist, userName) {
                $scope.performWorkflowAction(worklist, 'UNASSIGN', userName);
              };

              // handle workflow advancement
              $scope.handleWorkflow = function(worklist) {
                if ($scope.value == 'ASSIGNED'
                  && worklist
                  && (worklist.workflowStatus == 'NEW' || worklist.workflowStatus == 'READY_FOR_PUBLICATION')) {
                  $scope.performWorkflowAction(worklist, 'SAVE', $scope.user.userName);
                } else {
                  workflowService.fireWorklistChanged(worklist);
                }
              };

              // Performs a workflow action
              $scope.performWorkflowAction = function(worklist, action, userName) {

                workflowService.performWorkflowAction($scope.project.id, worklist.id, userName,
                  $scope.projects.role, action).then(function(data) {
                  workflowService.fireWorklistChanged(data);
                });
              };

 


              // Get the most recent note for display
              $scope.getLatestNote = function(worklist) {
                if (worklist && worklist.notes && worklist.notes.length > 0) {
                  return $sce.trustAsHtml(worklist.notes
                    .sort(utilService.sort_by('lastModified', -1))[0].value);
                }
                return $sce.trustAsHtml('');
              };


              $scope.getWorklists();

              //
              // MODALS
              //

              // Notes modal
              $scope.openNotesModal = function(lobject, ltype) {
                console.debug('openNotesModal ', lobject, ltype);

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/worklistTable/notes.html',
                  controller : NotesModalCtrl,
                  backdrop : 'static',
                  resolve : {
                    object : function() {
                      return lobject;
                    },
                    type : function() {
                      return ltype;
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

              // Notes controller
              var NotesModalCtrl = function($scope, $uibModalInstance, $sce, object, type,
                tinymceOptions) {
                console.debug('Entered notes modal control', object, type);
                $scope.object = object;
                $scope.type = type;
                $scope.tinymceOptions = tinymceOptions;
                $scope.newNote = null;

                // Paging parameters
                $scope.pageSize = 5;
                $scope.pagedNotes = [];
                $scope.paging = {};
                $scope.paging['notes'] = {
                  page : 1,
                  filter : '',
                  typeFilter : '',
                  sortField : 'lastModified',
                  ascending : true
                };
                $scope.errors = [];

                // Get paged notes (assume all are loaded)
                $scope.getPagedNotes = function() {
                  $scope.pagedNotes = utilService.getPagedArray($scope.object.notes,
                    $scope.paging['notes'], $scope.pageSize);
                };

                $scope.getNoteValue = function(note) {
                  return $sce.trustAsHtml(note.value);
                };

                // remove note
                $scope.removeNote = function(object, note) {

                  if ($scope.type == 'Worklist') {
                    workflowService.removeWorklistNote(object.id, note.id).then(
                    // Success - add worklist
                    function(data) {
                      $scope.newNote = null;
                      workflowService.getWorklist(object.id).then(function(data) {
                        object.notes = data.notes;
                        $scope.getPagedNotes();
                      },
                      // Error - add worklist
                      function(data) {
                        handleError($scope.errors, data);
                      });
                    },
                    // Error - add worklist
                    function(data) {
                      handleError($scope.errors, data);
                    });
                  } else if ($scope.type == 'Record') {
                    workflowService.removeWorklistRecordNote(object.id, note.id).then(
                    // Success - add worklist
                    function(data) {
                      $scope.newNote = null;
                      workflowService.getRecord(object.id).then(function(data) {
                        object.notes = data.notes;
                        $scope.getPagedNotes();
                      },
                      // Error - add worklist
                      function(data) {
                        handleError($scope.errors, data);
                      });
                    },
                    // Error - add worklist
                    function(data) {
                      handleError($scope.errors, data);
                    });
                  }
                };

                // add new note
                $scope.submitNote = function(object, text) {

                  if ($scope.type == 'Worklist') {
                    workflowService.addWorklistNote(object.id, text).then(
                    // Success - add worklist
                    function(data) {
                      $scope.newNote = null;
                      workflowService.getWorklist(object.id).then(function(data) {
                        object.notes = data.notes;
                        $scope.getPagedNotes();
                      },
                      // Error - add worklist
                      function(data) {
                        handleError($scope.errors, data);
                      });
                    },
                    // Error - add worklist
                    function(data) {
                      handleError($scope.errors, data);
                    });
                  } else if ($scope.type == 'Record') {
                    workflowService.addWorklistRecordNote(object.worklistId, object.id, text).then(
                    // Success - add worklist
                    function(data) {
                      $scope.newNote = null;

                      workflowService.getRecord(object.id).then(function(data) {
                        object.notes = data.notes;
                        $scope.getPagedNotes();
                      },
                      // Error - add worklist
                      function(data) {
                        handleError($scope.errors, data);
                      });
                    },
                    // Error - add worklist
                    function(data) {
                      handleError($scope.errors, data);
                    });
                  }
                };

                // Convert date to a string
                $scope.toDate = function(lastModified) {
                  return utilService.toDate(lastModified);
                };

                // Dismiss modal
                $scope.cancel = function() {
                  $uibModalInstance.dismiss('cancel');
                };

                // initialize modal
                $scope.getPagedNotes();
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
                    assignedUsers : function() {
                      return lworklist.authors;
                    },
                    project : function() {
                      return $scope.project;
                    },
                    role : function() {
                      if (lrole) {
                        return lrole;
                      } /*else {
                        return $scope.projects.role;
                      }*/
                    },
                    tinymceOptions : function() {
                      return utilService.tinymceOptions;
                    }
                  }

                });

                modalInstance.result.then(
                // Success
                function(data) {
                  workflowService.fireWorklistChanged(data);
                });
              };

              // Assign worklist controller
              var AssignWorklistModalCtrl = function($scope, $uibModalInstance, $sce, worklist, action,
                currentUser, assignedUsers, project, role, tinymceOptions) {
                console.debug('Entered assign worklist modal control', assignedUsers, project.id);
                $scope.worklist = worklist;
                $scope.action = action;
                $scope.project = project;
                $scope.role = role;
                $scope.tinymceOptions = tinymceOptions;
                $scope.assignedUsers = [];
                $scope.user = utilService.findBy(assignedUsers, currentUser, 'userName');
                $scope.note;
                $scope.errors = [];

                // Sort users by name and role restricts
                var sortedUsers = assignedUsers.sort(utilService.sort_by('name'));
                for (var i = 0; i < sortedUsers.length; i++) {
                  if ($scope.role == 'AUTHOR'
                    || $scope.project.userRoleMap[sortedUsers[i].userName] == 'REVIEWER'
                    || $scope.project.userRoleMap[sortedUsers[i].userName] == 'ADMIN') {
                    $scope.assignedUsers.push(sortedUsers[i]);
                  }
                }

                // Assign (or reassign)
                $scope.assignWorklist = function() {
                  if (!$scope.user) {
                    $scope.errors[0] = 'The user must be selected. ';
                    return;
                  }

                  if (action == 'ASSIGN') {
                    workflowService.performWorkflowAction($scope.project.id, worklist.id,
                      $scope.user.userName, $scope.role, 'ASSIGN').then(
                    // Success
                    function(data) {

                      // Add a note as well
                      if ($scope.note) {
                        workflowService.addWorklistNote(worklist.id, $scope.note).then(
                        // Success
                        function(data) {
                          $uibModalInstance.close(worklist);
                        },
                        // Error
                        function(data) {
                          handleError($scope.errors, data);
                        });
                      }
                      // close dialog if no note
                      else {
                        $uibModalInstance.close(worklist);
                      }

                    },
                    // Error
                    function(data) {
                      handleError($scope.errors, data);
                    });
                  }

                  // else reassign
                  else if (action == 'REASSIGN') {
                    workflowService.performWorkflowAction($scope.project.id, worklist.id,
                      $scope.user.userName, 'AUTHOR', 'REASSIGN').then(
                    // success - reassign
                    function(data) {
                      // Add a note as well
                      if ($scope.note) {
                        workflowService.addWorklistNote(worklist.id, $scope.note).then(
                        // Success - add note
                        function(data) {
                          $uibModalInstance.close(worklist);
                        },
                        // Error - remove note
                        function(data) {
                          handleError($scope.errors, data);
                        });
                      }
                      // close dialog if no note
                      else {
                        $uibModalInstance.close(worklist);
                      }
                    },
                    // Error - reassign
                    function(data) {
                      handleError($scope.errors, data);
                    });
                  }

                  // else unassign, then reassign
                  else if (action == 'UNASSIGN-REASSIGN') {
                    workflowService.performWorkflowAction($scope.project.id, worklist.id,
                      $scope.user.userName, $scope.role, 'UNASSIGN').then(
                      // Success - unassign
                      function(data) {
                        // The username doesn't matter - it'll go back to the
                        // author
                        workflowService.performWorkflowAction($scope.project.id, worklist.id,
                          $scope.user.userName, 'AUTHOR', 'REASSIGN').then(
                        // success - reassign
                        function(data) {
                          // Add a note as well
                          if ($scope.note) {
                            workflowService.addWorklistNote(worklist.id, $scope.note).then(
                            // Success - add note
                            function(data) {
                              $uibModalInstance.close(worklist);
                            },
                            // Error - remove note
                            function(data) {
                              handleError($scope.errors, data);
                            });
                          }
                          // close dialog if no note
                          else {
                            $uibModalInstance.close(worklist);
                          }
                        },
                        // Error - reassign
                        function(data) {
                          handleError($scope.errors, data);
                        });
                      },
                      // Error - unassign
                      function(data) {
                        handleError($scope.errors, data);
                      });
                  }
                };

                // Dismiss modal
                $scope.cancel = function() {
                  $uibModalInstance.dismiss('cancel');
                };

              };

              // Log modal
              $scope.openLogModal = function() {
                console.debug('openLogModal ');

                var modalInstance = $uibModal.open({
                  templateUrl : 'app/component/worklistTable/log.html',
                  controller : LogModalCtrl,
                  backdrop : 'static',
                  size : 'lg',
                  resolve : {
                    worklist : function() {
                      return $scope.selected.worklist;
                    },
                    project : function() {
                      return $scope.project;
                    }
                  }
                });

                // NO need for result function - no action on close
                // modalInstance.result.then(function(data) {});
              };

              // Log controller
              var LogModalCtrl = function($scope, $uibModalInstance, worklist, project) {
                console.debug('Entered log modal control', worklist, project);

                $scope.errors = [];
                $scope.warnings = [];

                // Get log to display
                $scope.getLog = function() {
                  projectService.getLog(project.id, worklist.id).then(
                  // Success
                  function(data) {
                    $scope.log = data;
                  },
                  // Error
                  function(data) {
                    handleError($scope.errors, data);
                  });

                };

                // Dismiss modal
                $scope.close = function() {
                  // nothing changed, don't pass a worklist
                  $uibModalInstance.close();
                };

                // initialize
                $scope.getLog();
              };

              // end

            } ]
        };
      } ]);
