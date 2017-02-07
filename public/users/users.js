'use strict';

angular.module('dashboardApp')
  .config(function ($stateProvider) {
    $stateProvider
      .state('user', {
        url: '/user',
        templateUrl: 'app/users/users.html',
        sp: {
            authorize: {
                group: 'users'
            }
        }
      });
  });