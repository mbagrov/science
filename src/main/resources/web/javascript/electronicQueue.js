/**
 * Created by alex on 14.02.16.
 */
var app = angular.module('ElectronicQueue', ['ui.bootstrap', 'ngSanitize'/*, 'reCAPTCHA', 'ui.mask'*/]);

/*app.config(['uiMaskConfig', function(uiMaskConfigProvider) {
    uiMaskConfigProvider.maskDefinitions({
        'A': /[a-z]/,
        '*': /[a-zA-Z0-9]/,
        '9': /\d/,
        'PhCode': /[1-9]{3,5}/,
        'PhNum': /[1-9]{5,7}/
    });
}]);*/
/*
app.config(function(reCAPTCHAProvider){

    reCAPTCHAProvider.setPublicKey('6LeHECETAAAAAEn16Lj6y2B0lfl8FGs12Ujzblxp');

    // optional: gets passed into the Recaptcha.create call
    reCAPTCHAProvider.setOptions({
        theme: 'clean'
    });
});
*/
app.controller('ElectronicQueueCtrl', ['$scope', '$http', function($scope, $http) {

    //input mask block
    $(document).ready(function(){
        Inputmask().mask(document.querySelectorAll("input"));
    });

    $("#email").inputmask({
        mask: "*{1,20}[.*{1,20}][.*{1,20}][.*{1,20}]@*{1,20}.*{1,20}[.*{2,6}][.*{1,2}]",
        greedy: false,
        onBeforePaste: function (pastedValue, opts) {
            pastedValue = pastedValue.toLowerCase();
            return pastedValue;
        },
        definitions: {
            '*': {
                validator: "[0-9A-Za-z!#$%&'*+/=?^_`{|}~\-]",
                cardinality: 1,
                casing: "lower"
            }
        },
        onincomplete: function(){
            $scope.electronicQueueForm.email.$invalid = true;
        },
        oncomplete: function(){
            $scope.electronicQueueForm.email.$invalid = false;
        }
    });

    var dateToString = function(date) {
        var monthWithZeroesPadded = ('0' + (date.getMonth() + 1)).slice(-2);
        var dateWithZeroesPadded = ('0' + (date.getDate())).slice(-2);
        return date.getFullYear() + "-" + monthWithZeroesPadded + "-" + dateWithZeroesPadded;
    };

    var getOrgAjaxLocation = function () {
        var inn = $scope.fields.inn;
        return $scope.fields.inn.length >= 3 ? '/organization?inn=' + inn : '/organization';
    };
    //$scope.random = Math.random();
    //$scope.capthaSourceUri = "http://www.google.com/recaptcha/api/js/recaptcha_ajax.js?random=" + Math.random();

    //var captchaPublicKey = "6LfQJ9gSAAAAAPwOfjjbcGEjfG4J8jHtN_vIxlyZ";
    $scope.actionUrl = '/submitElectronicQueueForm';

    $scope.submitted = false;

    $scope.incorrectCode = false;

    $scope.fields = {
        serviceType: 'license',
        inn: "",
        receptionDate: "",
        branchesCount: "0",
        org: "",
        orgTitle: "",
        orgFullTitle: "",
        licenseReasons: {},
        lastName: "",
        firstName: "",
        middleName: "",
        phone: "",
        mobilePhone: "",
        email: "",
        receptionTime: ""/*,
        userCaptcha: {"challenge": "", "response": ""}*/
    };

    $scope.orgs = [];
    $scope.queueReasons = [];
    $scope.serviceBasises = [];
    $scope.licenseReasons = [];
    $scope.numbersOfDocumentsList = [];
    $scope.programsNumbersList = [];
    $scope.issueDocumentAmountList = [];
    $scope.receptionSchedule = {};
    $scope.receptionTimeList = [];


    $scope.isformPart2 = false;
    $scope.timeIsSelected = false;
    $scope.isFormPart3 = false;
    $scope.noReceptionTime = false;
    $scope.organizationAlreadyScheduled = false;
    $scope.wrongEmail = false;

    //TODO: Подтягивание данных одной моделькой
    /*$scope.initFormValues = function () {
        $scope.getQueueReasons();

        $scope.getServiceBasis();
        $scope.getLicenseReasons();

        $scope.getProgramsNumbersList();
        $scope.getNumbersOfDocumentsList();
        $scope.getIssueDocumentAmountList();

        var url = '/initElectronicQueue?serviceType=' + $scope.fields.serviceType;
        $http.get(url).success(function(result) {
            $scope.queueReasons = result.queueReasons;
            $scope.fields.queueReason = result.queueReasons[0].code;
            //$scope.getServiceBasis();
        });
    };*/

    $scope.getQueueReasons = function(){
        var url = '/electronicQueueReason?serviceType=' + $scope.fields.serviceType;
        $http.get(url).success(function(result){
            $scope.queueReasons = result;
            $scope.fields.queueReason = result[0].code;
            $scope.getServiceBasis();
        });
    };

    $scope.getServiceBasis = function(){
        var url = '/serviceBasis?queueReason=' + $scope.fields.queueReason;
        $http.get(url).success(function(result){
            console.log(result);
            $scope.serviceBasises = result;
            $scope.fields.serviceBasis = result[0].code;
            $scope.getLicenseReasons();
        });
    };

    $scope.getLicenseReasons = function(){
        console.log($scope.fields.serviceBasis);
        var url = '/licenseReason?serviceBasis=' + $scope.fields.serviceBasis;
        $http.get(url).success(function(result){
            $scope.licenseReasons = result;
            if($scope.licenseReasons.length == 1) $scope.fields.licenseReasons[$scope.licenseReasons[0].code] = true;
            $scope.updateSelectedLicenseReasons();
        });
    };

    $scope.getProgramsNumbersList = function () {
        var url = '/numberOfProgram';
        $http.get(url).success(function(result){
            $scope.programsNumbersList = result;
            $scope.fields.numberOfPrograms= result[0].code;
        });
    };

    $scope.getNumbersOfDocumentsList = function(){
        var url = '/numberOfDocuments';
        $http.get(url).success(function(result){
            $scope.numbersOfDocumentsList = result;
            $scope.fields.numberOfDocuments= result[0].code;
        });
    };

    $scope.getIssueDocumentAmountList = function () {
        var url = '/issueDocumentAmount';
        $http.get(url).success(function(result){
            $scope.issueDocumentAmountList = result;
            $scope.fields.issueDocumentAmount= result[0].code;
        });
    };

    $scope.getSceduleTime = function () {
        var scedule = $scope.receptionSchedule.queryEntryMap;
        var defaultScedule = $scope.receptionSchedule.defaultScedule;
        var selectedDate = dateToString($scope.fields.receptionDate);

        var receptionTimeList = [];

        if (selectedDate != "") {
            if (scedule.hasOwnProperty(selectedDate)) {
                receptionTimeList = scedule[selectedDate];
            } else receptionTimeList = defaultScedule;
        }

        $scope.receptionTimeList = receptionTimeList;
    };

    $scope.refreshOrgs = function() {
        if ($scope.fields.serviceBasis != 'lp_010_grant_of_license' && $scope.fields.inn.length > 3) {
            console.log("Refreshing if needed... inn: " + $scope.fields.inn);
            var $orgsSelectbox = $(".orgs-selectbox");

            $orgsSelectbox.select2("destroy");
            $orgsSelectbox.select2($scope.orgsSelectOptions);
        }
    };

    $scope.refreshQueueReasons = function(){
        console.log($scope.fields.serviceType);
        $scope.getQueueReasons();
    };

    $scope.isQueueReasonTeq1OrTeq3 = function(){
        if($scope.fields.queueReason){
            $scope.refreshFieldValues();
            return $scope.fields.queueReason == "teq_1_reception_license" || $scope.fields.queueReason == "teq_3_reception_accreditation"
        }else{
            return false;
        }
    };

    $scope.isQueueReasonTeq2OrTeq4 = function(){
        if($scope.fields.queueReason){
            $scope.refreshFieldValues();
            return $scope.fields.queueReason == "teq_2_delivery_license" || $scope.fields.queueReason == "teq_4_delivery_accreditation"
        }else{
            return false;
        }
    };

    $scope.isQueueReasonTeq1 = function(){
        if($scope.fields.queueReason){
            $scope.refreshFieldValues();
            return $scope.fields.queueReason == 'teq_1_reception_license';
        }else{
            return false;
        }
    };

    $scope.fieldsForNewOrg = function() {
        return $scope.isGrantLicenseReason() || $scope.isReorganizationMergerOrTransformation()
    };

    $scope.isGrantLicenseReason = function(){
        return $scope.fields.serviceBasis == 'lp_010_grant_of_license'
    };

    $scope.isReorganizationMergerOrTransformation = function(){
        function selectedLicenseReasonsFunc(obj){
            var reasonCodesArray = [];
            for(var key in obj){ if(obj[key]) reasonCodesArray.push(key) };

            return reasonCodesArray;
        }
        var reorganizationMergerCode = "rs_107";
        var reorganizationTransformationCode = "rs_105";

        var selectedReasons = selectedLicenseReasonsFunc($scope.fields.licenseReasons);

        return $.inArray(reorganizationMergerCode, selectedReasons) > -1 || $.inArray(reorganizationTransformationCode, selectedReasons) > -1;
    };

    //Начало настроек календаря
    $scope.toggleMin = function() {
        $scope.minDate = $scope.minDate ? null : new Date();
    };

    $scope.toggleMin();

    $scope.maxDate = new Date(2020, 5, 22);

    $scope.disabled = function(date, mode) {
        var dateToCheckFormatted = dateToString(date);
        var scedule = $scope.receptionSchedule.queryEntryMap;
        var daysToDisable = $scope.receptionSchedule.daysToDisable;
        if (daysToDisable.indexOf(date.getDay()) > -1) return true;
        else
        {
            var disabledFlag = false;
            $.each(scedule, function(key, value) {
                if (key === dateToCheckFormatted && value.length === 0) disabledFlag = true;
            });
            return disabledFlag;
        }
    };

    $scope.dateOptions = {
        formatYear: 'yy',
        startingDay: 1
    };

    $scope.popup2 = {
        opened: false
    };

    $scope.open2 = function() {
        $scope.popup2.opened = true;
    };

    //конец календаря

    $scope.licenseReasonsErrors = function () {
        ($scope.fields.licenseReasons == undefined || Object.keys($scope.fields.licenseReasons).length == 0) &&
            $scope.isQueueReasonTeq1();
    };

    $scope.toFormPart2 = function () {
        var url = '/toStep2Form';
        $http({
            method: 'POST',
            url: url,
            data: JSON.stringify($scope.fields)
        }).success(function(data) {
            $("#toStep2ErrorMessage").empty();
            var period = data.period;
            var roomNumber = data.roomNumber;

            $("#period").text(period);
            $("#roomNumber").text(roomNumber);

            if (data.schedule == undefined) {
                $scope.noReceptionTime = true;
            } else {
                $scope.receptionSchedule = data.schedule;
                var parsedDate = new Date();
                parsedDate.setTime(Date.parse(data.schedule.maxDate));
                $scope.maxDate = parsedDate;
                $scope.isformPart2 = true;
            }
        }).error(function(message, code) {
            $("#toStep2ErrorMessage").empty();
            var badRequestMessage = "Произошла ошибка на стороне сервера.<br>" +
                code + " " + message + "<br>" +
                "Пожалуйста, повторите попытку.";
            $("#toStep2ErrorMessage").append($.parseHTML(badRequestMessage));
        });
    };

    $scope.updateShcedule = function () {
        var url = '/setShcedule';
        $http({
            method: 'POST',
            url: url,
            data: JSON.stringify($scope.fields)
        }).success(function(schedule) {
            $scope.receptionSchedule = schedule;
            var parsedDate = new Date();
            parsedDate.setTime(Date.parse(schedule.maxDate));
            $scope.maxDate = parsedDate;
            $scope.getSceduleTime();
        });
    };

    $scope.toFormPart1 = function () {
        //При нажатии кнопки "Назад" чистим выбранные дату и время
        $scope.fields.receptionDate = "";
        $scope.receptionTimeList = [];
        $scope.receptionTime = "";

        $scope.isformPart2 = false;
        $scope.submitted = false;
        $scope.timeIsSelected = false;
        $scope.noReceptionTime = false;
    };

    $scope.toStep2ButtonDisabled = function (form) {
        function licenseReasonNotChosen(obj){
            var foundTrueElem = false;
            for(var key in obj){ if(obj[key]) foundTrueElem = true };

            return !foundTrueElem;
        }
        return ($scope.fieldsForNewOrg() && ($scope.fields.orgTitle == "" || $scope.fields.orgTitle == undefined  ||
            $scope.fields.orgFullTitle == "" || $scope.fields.orgFullTitle == undefined || $scope.fields.inn == "" || $scope.fields.inn == undefined)) ||
            (!$scope.fieldsForNewOrg() && ($scope.fields.org == undefined || $scope.fields.org == "")) ||
            ($scope.fields.queueReason == "teq_1_reception_license" &&
                ($scope.fields.licenseReasons == undefined || licenseReasonNotChosen($scope.fields.licenseReasons))) ||
            form.queueReason.$invalid || form.serviceBasis.$invalid || form.numberOfPrograms.$invalid ||
            form.numberOfDocuments.$invalid || form.issueDocumentAmount.$invalid ||
            form.branchesCount.$invalid
    };

    $scope.updateSelectedLicenseReasons = function () {
        var selectedLicenseReasons = $scope.fields.licenseReasons;
        var licenseReasons = $scope.licenseReasons;
        $.each(selectedLicenseReasons, function(key, value) {
            var flag = true;
            $.each(licenseReasons, function(i, element) {
                if (element.code == key) {flag = false; return false;}
            });
            if (flag || !value) delete $scope.fields.licenseReasons[key];
        });

    };

    $scope.refreshFieldValues = function () {
        if ($scope.fields.queueReason != "teq_1_reception_license" && $scope.fields.queueReason != "teq_3_reception_accreditation") {
            //Очищаем ненужные филды
            $scope.fields.branchesCount = "";
            $scope.fields.numberOfPrograms = "";
            $scope.fields.numberOfDocuments = "";

            //Заполняем нужные дефолтными значениями
            if ($scope.fields.issueDocumentAmount == "" && $scope.issueDocumentAmountList.length != 0) $scope.fields.issueDocumentAmount = $scope.issueDocumentAmountList[0].code;
        }
        else if ($scope.fields.queueReason != "teq_2_delivery_license" && $scope.fields.queueReason != "teq_4_delivery_accreditation") {
            //Очищаем ненужные филды
            $scope.fields.issueDocumentAmount = "";

            //Заполняем нужные дефолтными значениями
            if ($scope.fields.branchesCount == "") $scope.fields.branchesCount = "0";
            if ($scope.fields.numberOfPrograms == "" && $scope.programsNumbersList.length != 0) $scope.fields.numberOfPrograms = $scope.programsNumbersList[0].code;
            if ($scope.fields.numberOfDocuments == "" && $scope.numbersOfDocumentsList.length != 0) $scope.fields.numberOfDocuments = $scope.numbersOfDocumentsList[0].code;
        }
    };

    $scope.orgsSelectOptions = {
        language: "ru",
        ajax: {
            url: getOrgAjaxLocation(),
            dataType: 'json',
            delay: 250,
            data: function (params) {
                return {
                    search: params.term,
                    page: params.page
                };
            },
            processResults: function (data, params) {
                params.page = params.page || 1;

                $scope.orgs = data.orgs;
                return {
                    results: data.orgs,
                    pagination: {
                        more: (params.page * 10) < data.count
                    }
                };
            },
            cache: true
        },
        initSelection: function(element, callback) {
            $.ajax('/organization?inn=' + $scope.fields.inn, {
                dataType: "json"
            }).done(function(data) {
                $scope.fields.org = data.orgs.length == 0 ? "" : data.orgs[0].id;
                callback(data.orgs[0]);
            });
        },
        escapeMarkup: function (markup) { return markup; },
        minimumInputLength: 1
    };

    $(".orgs-selectbox").select2($scope.orgsSelectOptions);

    $(".orgs-selectbox").on("change", function (e) {
        var foundOrgs = $.grep($scope.orgs, function (org) {
            return org.id == e.target.value;
        });
        if (foundOrgs.length > 0) $scope.fields.inn = foundOrgs[0].inn;
    });

    $scope.submitForm = function () {
        $scope.submitted = true;

        if ($scope.electronicQueueForm.$valid && !$scope.electronicQueueForm.email.$invalid) {
            var fieldsCopy = angular.copy($scope.fields);
            var date = fieldsCopy.receptionDate;
            fieldsCopy.receptionDate = dateToString(date);
            $scope.clearOrg(fieldsCopy);
            //fieldsCopy.userCaptcha = $scope.userCaptcha;
            $http({
                method: 'POST',
                url: $scope.actionUrl,
                data: JSON.stringify(fieldsCopy)
            }).success(function(data) {
                if (data.submited == "timeSelected") {
                    $scope.timeIsSelected = true;
                    $scope.updateShcedule();
                } else if(data.submited == "organizationAlreadyScheduled") {
                    $scope.organizationAlreadyScheduled = true;
                }
                /*else if (data.submited == "wrongCaptcha") {
                    $("#errorMesage").empty();
                    $("#errorMesage").append("Неправильно введен код");
                    $scope.incorrectCode = true;
                    Recaptcha.reload();
                }*/
                else {
                    var successMessage = "Запись на прием " + data.message.receptionDate + " в " + data.message.receptionTime +
                    "(кабинет № " + data.message.roomNumber + ")<br>" +
                    "Продолжительность приема составит приблизительно " + data.message.period + " минут<br>" +
                    "Тип услуги: " + data.message.serviceBasis + "<br>" +
                    "Причина обращения: " + data.message.licenseReasons + "<br>" +
                    "Дата осуществления записи: " + data.message.currentDate + "<br><br>" +
                    "Внимание! Для подтверждения регистрации Вам необходимо пройти по ссылке, указанной в письме.<br>" +
                    "При отсутствии подверждения Ваша запись будет отменена в течение 3 дней.";
                    var $confirmedMessageBox = $("#confirmedMessage");
                    $confirmedMessageBox.empty();
                    $confirmedMessageBox.append($.parseHTML(successMessage));

                    $scope.timeIsSelected = false;
                    $scope.isFormPart3 = true;
                }
            }).error(function(message, code) {
                if(code == '400' && message.submited == "wrongEmail"){ $scope.wrongEmail  = true; }
                else {
                    var badRequestMessage = "Произошла ошибка.<br>" +
                        code + " " + message;
                    $(".container").append($.parseHTML(badRequestMessage));
                }
            });
        }
    };

    $scope.clearOrg = function(fields) {
        if ($scope.fieldsForNewOrg()) {
            fields.org = null;
        } else {
            fields.orgFullTitle = null;
            fields.orgTitle = null;
        }
    };

    $scope.reload = function() {
        window.location.reload();
    };

    /*$scope.isCaptchaEmpty = function() {
        return ($scope.userCaptcha.challenge == "" || $scope.userCaptcha.response == "" || $scope.userCaptcha.challenge == null || $scope.userCaptcha.response == null)
            && !$scope.incorrectCode
    };*/

    $scope.noTimeOrFormPart2 = function() {
        return $scope.isformPart2 || $scope.noReceptionTime;
    };

    $scope.getQueueReasons();
    $scope.getProgramsNumbersList();
    $scope.getNumbersOfDocumentsList();
    $scope.getIssueDocumentAmountList();
}]);