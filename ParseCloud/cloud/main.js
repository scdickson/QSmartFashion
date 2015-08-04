Parse.Cloud.afterSave("DataMeasurement", function(request, response) 
{
	var twilio = require("twilio");
	twilio.initialize("AC9b6556e0210c805213a6b6a22547f14d","f73a18e62798d9b69aea7c00aa0dfd11");


	var heartrate = request.object.get("heartrate");
  	var temp = request.object.get("temperature");
	var user = request.object.get("user");
	var alertSOS = false;


	//Get a human-readable address for a lat, lng pair
	var lat = request.object.get("lat");
	var lng = request.object.get("lng");
	if(lat != null && lng != null)
	{
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
	            });
   }

	var query = new Parse.Query(Parse.User);
	query.get(request.object.get('user').id, {
	    success: function(user) 
	    {
	    	//console.log("Calculating values for user  '" + user.get("name") + "'");
    		var now = new Date();
    		var utc1 = Date.UTC(now.getFullYear(), now.getMonth(), now.getDate());
  			var utc2 = Date.UTC(user.get("birthdate").getFullYear(), user.get("birthdate").getMonth(), user.get("birthdate").getDate());
			var age = Math.abs(Math.floor((utc1 - utc2) / (1000 * 60 * 60 * 24 * 365)));

			//console.log("User is " + user.get("sex") + " and " + age + " years old");

			var max_heartrate;
			var min_heartrate;

			if(user.get("sex") == "F")
			{
				max_heartrate = 203.7 / (1 + Math.exp(0.033 * (age - 104.3)));
			}
			else if(user.get("sex") == "M")
			{
				max_heartrate = 190.2 / (1 + Math.exp(0.0453 * (age - 107.5)));
			}
			min_heartrate = max_heartrate * 0.35;
			//console.log("Max heartrate is " + max_heartrate + " bpm");
			//console.log("Min heartrate is " + min_heartrate + " bpm");

			if(heartrate <= min_heartrate || heartrate >= max_heartrate)
			{
				//console.log("Heartrate falls outside acceptable range!");
				alertSOS = true;
			}

			//console.log("Max Temperature is " + (37.5 + (37.5*0.1)) + " C");
			//console.log("Min Temperature is " + (36.5 - (36.5*0.1)) + " C");
			if(temp <= (36.5 - (36.5*0.1)) || temp >= (37.5 + (37.5*0.1)))
			{
				//console.log("Temperature falls outside acceptable range!");
				alertSOS = true;
			}

			if(alertSOS)
			{
				var SOSContact = Parse.Object.extend("EmergencyContact");
				var sos_contact_query = new Parse.Query(SOSContact);
				sos_contact_query.equalTo("user", request.object.get('user'));
				sos_contact_query.find({
		    		success: function(results) 
		    		{
		    			for (var i = 0; i < results.length; i++) 
		    			{
      						var object = results[i];
      						twilio.sendSMS({
							    From: "+17656370636",
							    To: object.get('phoneNumber'),
							    Body: user.get('name') + "is in danger!"
							  	}, {
							    	success: function(httpResponse) { },
							    	error: function(httpResponse) { }
							  });
      					}
		    		},
		    		error: function(error) {}
	    		});
			}


	    },
	    error: function() {}
	});
	
});
