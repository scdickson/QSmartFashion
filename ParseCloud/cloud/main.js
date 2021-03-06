Parse.Cloud.afterSave("DataMeasurement", function(request, response) 
{
	console.log("--------------------------------------------------------------");
	var twilio = require("twilio");
	twilio.initialize(APP_KEY,SECRET_KEY);


	var heartrate = request.object.get("heartrate");
  	var temp = request.object.get("temperature");
	var user = request.object.get("user");
	var alertSOS = false;
	var didGmaps = false;
	var formattedAddress;


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
	                    key: "GMAPS_KEY"
	                },
	                success: function(httpResponse) 
	                {
	                    var response=httpResponse.data;
	                    if(response.status == "OK" && !didGmaps){
	                    	didGmaps = true;
							formattedAddress = response.results[0].formatted_address;
							//request.object.set("HRLocation", formattedAddress);
							//request.object.save();
							var query = new Parse.Query(Parse.User);
							query.get(request.object.get('user').id, {
							    success: function(user) 
							    {
							    	console.log("Calculating values for user  '" + user.get("name") + "'");
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
										console.log("Heartrate falls outside acceptable range!");
										alertSOS = true;
									}

									//console.log("Max Temperature is " + (37.5 + (37.5*0.1)) + " C");
									//console.log("Min Temperature is " + (36.5 - (36.5*0.1)) + " C");
									if(temp <= (36.5 - (36.5*0.1)) || temp >= (37.5 + (37.5*0.1)))
									{
										console.log("Temperature falls outside acceptable range!");
										alertSOS = true;
									}

									if(alertSOS)
									{
										var SOSLogObject = Parse.Object.extend("SOSLog");
										var SOSLog = new SOSLogObject();
										SOSLog.set("user", request.object.get('user'));
										SOSLog.set("heartrate", heartrate);
										SOSLog.set("temperature", temp);
										SOSLog.set("HRLocation", formattedAddress);
										SOSLog.save(null, {
          									success: function(SOSLog) 
									          {
									        },
									          error: function(SOSLog, error) 
									          {
									          }
									      });


										var SOSMessage = "Your friend " + user.get('name') + " is in need of emergency assistance. "
										if(user.get("sex") == "F")
										{
											SOSMessage += "She";
										}
										else if(user.get("sex") == "M")
										{
											SOSMessage += "He";
										}
										SOSMessage += " is near " + formattedAddress + ". Please hurry!";

										var SOSContact = Parse.Object.extend("EmergencyContact");
										var sos_contact_query = new Parse.Query(SOSContact);
										sos_contact_query.equalTo("user", request.object.get('user'));
										sos_contact_query.find({
								    		success: function(results) 
								    		{
								    			for (var i = 0; i < results.length; i++) 
								    			{
						      						var object = results[i];
						      						console.log("Sending SOS Message to " + object.get('name'));
						      						twilio.sendSMS({
													    From: "+17656370636",
													    To: object.get('phoneNumber'),
													    Body: SOSMessage
													  	}, {
													    	success: function(httpResponse) { },
													    	error: function(httpResponse) { }
													  });
						      					}
						      					alertSOS = false;
								    		},
								    		error: function(error) {}
							    		});
									}


							    },
							    error: function() {}
							});
	                     }
	                },
	                error: function(httpResponse) {
	                    console.error('Request failed with response code ' + httpResponse.status);
	                }
	            });
   }
   else
   {
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
				var SOSMessage = "Your friend " + user.get('name') + " is in need of emergency assistance. Please hurry!"

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
							    Body: SOSMessage
							  	}, {
							    	success: function(httpResponse) { },
							    	error: function(httpResponse) { }
							  });
      					}
      					alertSOS = false;
		    		},
		    		error: function(error) {}
	    		});
			}


	    },
	    error: function() {}
	});
   }

	
	
});
