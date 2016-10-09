/**
 * Created by alex on 24.03.16.
 */
var licenseCard = angular.module('licenseCard', []);

licenseCard.controller('LicenseCardCtrl', ['$scope', '$http', function($scope, $http) {

    angular.element(document).ready(function(){
        $('#loading').remove();
    });

    $scope.licenseWrapper = {};
    console.log(window.licenseId);
    $http.get('/licenseData?id=' + window.licenseId).success(function(result){
        console.log(result);
        $scope.licenseWrapper = result;
    });
    
    $scope.togglePrograms = function(el){
        if(el.showPrograms) 
            el.showPrograms = false; 
        else 
            el.showPrograms = true;
    }
}]);