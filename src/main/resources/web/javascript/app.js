/**
 * Created by alex on 10.02.16.
 */
var app = angular.module('aggregatedRegistry', ['ui.bootstrap', 'angularSpinner']);

app.controller('AggregatedRegistrySearchCtrl', ['$scope', '$http', function($scope, $http) {

    angular.element(document).ready(function(){
        $('#loading').remove();
    });

    $scope.searchResult = [];
    //*Paging setup block*/
    $scope.currentPage = 1;
    $scope.totalItems = 10;
    $scope.itemsPerPage = 10;
    $scope.maxSize = 5;

    $scope.setPage = function (pageNo) {
        $scope.currentPage = pageNo;
    };

    $scope.pageChanged = function() {
        console.log('Page changed to: ' + $scope.currentPage);
        $scope.getSearchResult();
    };
    /******/

    /*Predefined model for select boxes*/
    $scope.data = {
      "regions": [{"code": "", "name": "Любой"}],
      "licenseOrgans": [
          {"code": "", "title": "Все лицензирующие органы"},
          {"code": "1322520", "title": "Федеральный лицензирующий орган"},
          {"code": "0000000", "title": "Региональные лицензирующие органы"} //нужно добавить код для регионального и его в систему и при импорте учитывать и простовлять
      ]
    };

    $scope.clearForm = function(){
        $scope.orgTitle = undefined;
        $scope.licenseOrgan = undefined;
        $scope.region = undefined;
        $scope.licenseRegNum = undefined;
        $scope.ogrn = undefined;
        $scope.inn = undefined;
        $scope.dateTo.value = undefined;
        $scope.dateFrom.value = undefined;

    };

    $scope.offset = function(){return ($scope.currentPage - 1) * $scope.itemsPerPage};

    $scope.getSearchResult = function(){

        var formData = {
            "region": $scope.region || undefined,
            "licenseOrgan": $scope.licenseOrgan || undefined,
            "orgTitle": $scope.orgTitle || undefined,
            "inn": $scope.inn || undefined,
            "ogrn": $scope.ogrn || undefined,
            "licenseRegNum": $scope.licenseRegNum || undefined,
            "licenseDeliveryDateFrom": $.format.date($scope.dateFrom.value, "yyyy-MM-dd") || undefined,
            "licenseDeliveryDateTo": $.format.date($scope.dateTo.value, "yyyy-MM-dd") || undefined,
            "offset": $scope.offset(),
            "pageLength":  $scope.itemsPerPage
        };
    
        $http({
            method: 'POST',
            url: '/data',
            data: JSON.stringify(formData)
        }).then(function successCallback(result){
                $scope.searchResult = result.data[0];
                $scope.searchResultTotal = result.data[1];
                $scope.totalItems = result.data[1];
            }, function errorCallback(response){
                console.log(response);
            }
        );
    };

    //*Get regions for select*/
    $http.get('/regions').success(function(result){
        jQuery.map(result, function(elem){
            $scope.data.regions.push(elem);
        });
    });

    $scope.stringifyState = function(stateCode){
      return stateCode === "VALID" ? "Действует" : "Не действует"
    };

    /*инициализация date picker, возможно заменить на директиву из ui*/
    $(".input-group.date").datepicker({ autoclose: true, todayHighlight: true, format: 'yyyy-mm-dd' });


    $scope.dateOptions = {
        formatYear: 'yyyy',
        f: 'yyyy',
        startingDay: 1
    };

    $scope.formats = ['yyyy-MM-dd', 'yyyy/MM/dd', 'dd.MM.yyyy', 'shortDate'];
    $scope.format = $scope.formats[0];

    $scope.openDateTo = function() {
        $scope.dateTo.opened = true;
    };

    $scope.openDateFrom = function() {
        $scope.dateFrom.opened = true;
    };

    $scope.dateTo = {
        opened: false,
        value: ""
    };

    $scope.dateFrom = {
        opened: false,
        value: ""
    };

    $scope.showLicenseCard = function(id){

        var licenseCardWindow = window.open("/license/licenseCard.html", "_blank", "toolbar=1, scrollbars=1, resizable=1, width=" + 1015 + ", height=" + 800);
        licenseCardWindow.licenseId = id;
    };

}]);