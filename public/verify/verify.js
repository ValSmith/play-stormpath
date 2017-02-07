'use strict';

angular.module('dashboardApp')
  .config(function ($stateProvider) {
    $stateProvider
      .state('verifyPage', {
        url: '/verifyPage?sptoken',
        templateUrl: 'app/verify/verify.html',
        controller: 'VerifyCtrl'
      });
  });