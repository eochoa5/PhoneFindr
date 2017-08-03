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

	//console.log('a user has connected: ' + socket.id);
	
	socket.email = socket.handshake.query['email'];

		
		//phone connection lets WebUI know that phone is connected
		if (socket.handshake.query.phone == "true"){

			socket.phoneName = socket.handshake.query.phoneName;

			for(i=0 ; i<users.length; i++){

			if(socket.email == users[i].email && users[i].id != socket.id){
				io.to(users[i].id).emit('phoneConnected', socket.handshake.query.phoneName);
				}
			}

		}
		else{
			//check if our phone is connected when we connect 

			for(i=0 ; i<users.length; i++){

			if(socket.email == users[i].email && users[i].id != socket.id){
				io.to(socket.id).emit('alreadyConnected', users[i].phoneName);
				}
			}

		}


		
	users.push(socket);

	socket.on('ring', function(data){

		for(i=0 ; i<users.length; i++){
			if(users[i].email == data.em && users[i].id != socket.id){

				io.to(users[i].id).emit('ring request', data.name);
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

		//console.log('a user has disconnected: ' + socket.id);
		if(socket.handshake.query.phone == "true"){
				//console.log("a phone has disconnected");

				for(i=0 ; i<users.length; i++){
					if(socket.email == users[i].email && users[i].id != socket.id){
						io.to(users[i].id).emit('phoneDisconnected');
						}
				}
			}


		//remove from array

		for(i=0 ; i<users.length; i++){

			if(users[i].id == socket.id){
				users.splice(i,1);
			}
		}

		

	});
	

});

http.listen(process.env.PORT || 8080, function(){
	console.log('server running');
});


