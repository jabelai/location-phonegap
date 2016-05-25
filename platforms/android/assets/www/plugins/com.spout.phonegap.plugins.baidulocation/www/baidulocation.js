cordova.define("com.spout.phonegap.plugins.baidulocation.BiaduLocation", function(require, exports, module) {
window.locationService = {
	execute: function(action, successCallback, errorCallback, params) {
		cordova.exec(    
			function(pos) {
				var errcode = pos.code;
				if (errcode == 61 || errcode == 65 || errcode == 161) {
					successCallback(pos);
				} else {
					if (typeof(errorCallback) != "undefined") {
						if (errcode >= 162) {
							errcode = 162;
						}
						errorCallback(pos)
					};
				}
			}, 
			function(err){},
			"BaiduLocation",
			action,
			[params]
		)
	},
	getCurrentPosition: function(successCallback, errorCallback, params) {
		this.execute("getCurrentPosition", successCallback, errorCallback, params);
	},
	stop: function(action, successCallback, errorCallback) {
		this.execute("stop", successCallback, errorCallback);
	},

	getCurrentPositionOnce: function(successCallback, errorCallback) {
	    this.execute("getCurrentPositionOnce", successCallback, errorCallback);
	}
}
module.exports = locationService;
});
