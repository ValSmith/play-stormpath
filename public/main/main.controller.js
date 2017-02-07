'use strict';

angular.module('dashboardApp')
  .controller('MainCtrl', function ($scope, $http) {
    $scope.awesomeThings = [];
      $scope.userData = "loading..";
      $scope.adminData = "loading..";

    $http.get('/api/things').success(function(awesomeThings) {
      $scope.awesomeThings = awesomeThings;
    });

      $http.get('/api/adminThings').success(function(adminThings) {
          $scope.adminData = adminThings;
      });

      $http.get('/api/userThings').success(function(userThings) {
          $scope.userData = userThings;
      });
  });
