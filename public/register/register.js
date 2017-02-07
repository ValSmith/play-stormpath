'use strict';

angular.module('dashboardApp')
  .config(function ($stateProvider) {
    $stateProvider
      .state('registerPage', {
        url: '/registerPage',
        templateUrl: 'app/register/register.html',
        controller: 'RegisterCtrl'
      });
  });