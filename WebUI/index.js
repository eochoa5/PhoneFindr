var express = require('express');
var app = express();
var http = require('http').Server(app);
var io = require('socket.io')(http);

app.get('/', function(req, res){
	res.sendFile(__dirname+'/index.html');

});

users = [];

app.use(express.static(__dirname));

io.on('connection', function(socket){

	console.log('a user has connected: ' + socket.id);
	
	socket.email = socket.handshake.query['email'];

		
		//phone connection lets WebUI know that phone is Connected
		if (socket.handshake.query.phone == "true"){
			console.log("phone connected");

			for(i=0 ; i<users.length; i++){

			if(socket.email == users[i].email && users[i].id != socket.id){
				io.to(users[i].id).emit('phoneConnected', socket.handshake.query.Name);
				}
			}

		}


		

	users.push(socket);

	socket.on('ring', function(data){

		for(i=0 ; i<users.length; i++){
			if(users[i].email == data && users[i].id != socket.id){

				io.to(users[i].id).emit('ring request', socket.id);
			}

		}
	});

	socket.on('requestLocation', function(data){

		for(i=0 ; i<users.length; i++){
			if(users[i].email == data && users[i].id != socket.id){

				io.to(users[i].id).emit('location request', socket.id);
			}

		}
	});

	socket.on('send location', function(data){
		io.to(data.to).emit('location received', {lat: data.lat, lon: data.lon});
		
	});


	socket.on('disconnect', function(){

		console.log('a user has disconnected: ' + socket.id);
		if(socket.handshake.query.phone == "true")
			{
				console.log("a phone has disconnected");

				for(i=0 ; i<users.length; i++){
					if(socket.email == users[i].email && users[i].id != socket.id){
						io.to(users[i].id).emit('phoneDisconnected');
						}
				}
			}

		for(i=0 ; i<users.length; i++){

			if(users[i].id == socket.id){
				users.splice(i,1);
			}
		}

		

	});

	socket.on('phoneDisconnected', function(){
		console.log('phone disconnected');
	})

	// socket.on('phoneConnected', function(data){
	// 	console.log('Phone Connected');

	// 	for(i=0 ; i<users.length; i++){

	// 		if(data.Email == users[i].email && users[i].id != socket.id){
	// 			console.log("socket.id");
	// 			io.to(users[i].id).emit('isPhoneConnected', socket.handshake.query.Name);
	// 			//find out how to do data.to, send the name
	// 			//io.to(data.to).emit('isPhoneConnected', {lat: data.lat, lon: data.lon});
	// 		}
	// 	}

	// });

});

http.listen(process.env.PORT || 8080, function(){
	console.log('server running');
});


