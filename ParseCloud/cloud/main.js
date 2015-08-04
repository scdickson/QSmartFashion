Parse.Cloud.afterSave("DataMeasurement", function(request, response) {
	var heartrate = request.object.get("heartrate");
  	var temp = request.object.get("temperature");
	var user = request.object.get("user");
	console.log("uid is " + user.objectId);
	
	/*var lat = request.object.get("lat");
	var lng = request.object.get("lng");

	//Get a human-readable address for a lat, lng pair
   	Parse.Cloud.httpRequest({
                method: "POST",
                url: 'https://maps.googleapis.com/maps/api/geocode/json',
                params: {
                    latlng : lat + "," + lng,
                    key: "AIzaSyCazMxcpH4l4HSwB_ofk8Nnm9aLTnAVyQI"
                },
                success: function(httpResponse) {
                    var response=httpResponse.data;
                    if(response.status == "OK"){
						var formattedAddress = response.results[0].formatted_address;
						request.object.set("HRLocation", formattedAddress);
						request.object.save();
						response.success();
						console.log(formattedAddress);
                     }
                },
                error: function(httpResponse) {
                    console.error('Request failed with response code ' + httpResponse.status);
                }
            });*/

	request.object.get('sender').fetch().then(function(user) 
	{
            var sender = user.get('username');
    }
    
	
	//Check if heartrate and temperature are within norms
	var now = new Date();
	var age = (Math.abs(user.birthdate - now)) / 1000 / 60 / 60 / 24 / 365;
	console.log("User is " + age);

	if(user.sex == "F")
	{
		var max_heartrate = 203.7 / (1 + Math.exp(0.033 * (age - 104.3)));
		console.log("Max HR is " + max_heartrate + " bpm");
	}
	else if(user.sex == "M")
	{
		var max_heartrate = 190.2 / (1 + Math.exp(0.0453 * (age - 107.5)));
		console.log("Max HR is " + max_heartrate + " bpm");
	}
	
});
