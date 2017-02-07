'use strict';

angular.module('dashboardApp')
  .config(function ($stateProvider) {
    $stateProvider
      .state('loginPage', {
        url: '/loginPage',
        templateUrl: 'app/login/login.html',
        controller: 'LoginCtrl'
      });
  });