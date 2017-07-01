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


	socket.on('new user', function(data){
		socket.email = data;
		users.push(socket);
		var phoneID;

		/*
		console.log("USERS: " );

		for(i=0 ; i<users.length; i++){
			console.log("email : " + users[i].email + " id: " + users[i].id);

		}
		*/
		
		
	});

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

				phoneID = users[i].email;
				io.to(users[i].id).emit('location request', socket.id);
				console.log("sent a location request");
			}

		}
	});

	socket.on('receiveLocation', function(data){
		console.log("received a location");
		console.log(data);

	for(i=0 ; i<users.length; i++){
		if(users[i].email == phoneID && users[i].id != socket.id){

				io.to(users[i].id).emit('locationPayload', socket.id);
				console.log("location payload moving out");
			}
		}

		
	});


	socket.on('disconnect', function(){

		console.log('a user has disconnected: ' + socket.id);

		users.splice(users.indexOf(socket), 1);
	});

});

http.listen(process.env.PORT || 8080, function(){
	console.log('server running');
});


