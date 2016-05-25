var gbOpenTimer = true;
var currentLon;
var currentLat;

var app = {
    // Application Constructor
    initialize: function() {
        this.bindEvents();
    },
    bindEvents: function() {
        document.addEventListener('deviceready', this.onDeviceReady, false);
    },
    onDeviceReady: function() {
        console.log("设备就绪");
    },


    startGPSAction: function() {
        gbOpenTimer = true;
        app.getGPS();

    },

    endGPSAction: function() {
        gbOpenTimer = false;
        app.stopGPS();
    },

    getGPSOnce: function() {
        window.locationService.getCurrentPositionOnce(app.getGPSSuccess, app.getGPSError);
    },

    getGPS: function() {
        /*
        启用 baidu 定位服务
         */
        if (gbOpenTimer) {
            window.locationService.getCurrentPosition(app.getGPSSuccess, app.getGPSError, "110");
        }
    },
    stopGPS: function() {
        /*
        关闭 baidu 定位服务
         */
        console.log("停止服务中");
        window.locationService.stop(app.stopGPSSuccess, app.stopGPSError);
    },

    //定时等待刷新
    //
    //需要一个纯时间等待方法
    waitRefresh: function(time) {
        if (gbOpenTimer) {
            console.log("刷新 - 定时等待");
            setTimeout (app.getGPS, time);
        }
    },

    getGPSSuccess: function(position){

    	/*
    	获取返回的 baidu 座标
    	 */
        console.log("获取返回的 baidu 座标");
        currentLon = position.coords.longitude;
        currentLat = position.coords.latitude;
        var currentRadius = position.coords.radius;
        console.log("获取转换后坐标及精度:" + currentLon + "," + currentLat + "," + currentRadius);

        /*
        网页显示返回结果
         */
        document.getElementById("position").innerHTML +=(app.formatFloat(currentLon,5)+"," + app.formatFloat(currentLat,5) + "," + currentRadius + "; ");
        //document.getElementById("position").innerHTML +=("GPS Loc:" + currentLon.toFixed(6)+"," + currentLat.toFixed(6) + "," + currentRadius.toFixed(2) + "<br/>");

        var currentLocationType = position.locationType;
        console.log("获取方式:" + currentLocationType);

        if (currentLocationType == 61) {
            var currentSpeed = position.coords.speed;
            var currentSatelliteNumber = position.SatelliteNumber;

            console.log("获取速度:" + currentSpeed);
            console.log("获取卫星数:" + currentSatelliteNumber);

            /*
            网页显示返回结果
             */
            document.getElementById("Satellite").innerHTML +=(currentSatelliteNumber +"," + app.formatFloat(currentSpeed,5) + "; ");

        }

    	/*
    	判断数据 卫星GPS定位
    	 */

    	if ((currentLocationType == '61') && (currentRadius <= '30') && (currentSatelliteNumber >= '3') && (currentSpeed > '0')) {
    	    console.log("判断条件成立:GPS定位,精度小于30,卫星数大于2.");
//    	    if (networkState2 == '4') {
            if ((navigator.connection.type == Connection.CELL_2G) ||
                (navigator.connection.type == Connection.CELL_3G) ||
                (navigator.connection.type == Connection.CELL_4G) ||
                (navigator.connection.type == Connection.CELL)) {
                /*
        	    传送获得的 baidu 座标
        	     */
        	    console.log("开始上传坐标");
    	        app.transferGPSPoint(currentLon,currentLat);
        	    /*
        	    定时等待刷新
        	     */
               app.waitRefresh(3000);

    	    } else {
    	        console.log("非手机网络,重新获取座标.");
                /*
        	    定时等待刷新
        	     */
    	        app.waitRefresh(3000);
    	    }
    	} else {
    	    console.log("判断条件失败:重新获取 baidu 座标.");
    	    console.log(navigator.connection.type);

            /*
        	定时等待刷新
        	 */
    	    app.waitRefresh(3000);
    	}

    	/*
    	传送获得的 baidu 座标
    	 */
//    	app.transferGPSPoint(currentLon,currentLat);

    	/*
    	定时等待刷新
    	 */
//    	app.waitRefresh(3000);

    },
    getGPSError: function(error){

    	/*
    	获取返回的 baidu 错误
    	 */
    	console.log("报错信息");
    	if (error == 68) {
    	console.log("无网络");
    	} else {
    	console.log("其他错误" + error);
    	}

    	app.waitRefresh(3000);

    },
    stopGPSSuccess: function(s){

    	/*
    	获取返回的 baidu 停止结果
    	 */
    	//console.log(e);
    	console.log("停止成功");

    },
    stopGPSError: function(e){

    	/*
    	获取返回的 baidu 停止结果
    	 */
    	//console.log(e);
    	console.log("停止失败");

    },
    //传送坐标
    transferGPSPoint: function(currentLon, currentLat) {
        var url="http://tangbos.3322.org:8090/gps/gpsuserajax/gpsuser_add2.action?mapX="+currentLon+"&mapY="+currentLat;
        console.log("传送坐标 - 地址设定成功");

            $.ajax({
                   type: "get",        //使用get方法访问后台
                   dataType: 'jsonp',
                   jsonp: 'callback',
                   url: url,
            });
        console.log("传送坐标 - 数据上传成功");
    },
    formatFloat: function(src, pos) {
        return Math.round(src*Math.pow(10, pos))/Math.pow(10, pos);
    },

    //网络检测
    networkState: function() {
        var networkState = navigator.connection.type;

        console.log("网络状态检测");

        if (networkState == Connection.NONE) {
            console.log("无网络");
        } else if (networkState == Connection.UNKNOWN) {
            console.log("未知网络");
        } else if (networkState == Connection.ETHERNET) {
            console.log("本地网络");
        } else if (networkState == Connection.WIFI){
            //alert("请不要在室内巡检.");
        } else {
            console.log("网络正常");
            //app.getGPS();
        }
    }

//    //网络检测
//    networkState2: function() {
//        var networkState = navigator.connection.type;
//
//        console.log("网络状态检测");
//
//        if (networkState == Connection.NONE) {
//            console.log("无网络");
//            return 0;
//        } else if (networkState == Connection.UNKNOWN) {
//            console.log("未知网络");
//            return 1;
//        } else if (networkState == Connection.ETHERNET) {
//            console.log("本地网络");
//            return 2;
//        } else if (networkState == Connection.WIFI){
//            //alert("请不要在室内巡检.");
//            return 3;
//        } else {
//            console.log("网络正常");
//            return 4;
//            //app.getGPS();
//        }
//    }
};

app.initialize();