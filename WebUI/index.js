var express = require('express');
var app = express();
var http = require('http').Server(app);
var io = require('socket.io')(http);

users = [];
connections = [];

app.get('/', function(req, res){
	res.sendFile(__dirname+'/index.html');

});

users = [];

app.use(express.static(__dirname));
//drew2g 6/23 ===============================
//push socket to connection array and console log it
//===========================================

io.on('connection', function(socket){
// <<<<<<< WebUI
	connections.push(socket);
	console.log('[C] %s sockets connected. New user %s', connections.length, socket.id);
//=======

// 	console.log('a user has connected: ' + socket.id);
// >>>>>>> master


	socket.on('new user', function(data){
		socket.email = data;
		users.push(socket);

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

	socket.on('disconnect', function(){
// <<<<<<< WebUI
		console.log('[D] User %s has disconnected', socket.id);
	});

	socket.on('new user', function(email, callback){
		if(users.indexOf(email) != -1){
			callback(false);
			return;
		}	
		callback(true);
		socket.email = email;
		users.push(socket.email);
// =======

// 		console.log('a user has disconnected: ' + socket.id);

		users.splice(users.indexOf(socket), 1);
// >>>>>>> master
	});

});

http.listen(process.env.PORT || 3000, function(){
	console.log('server running');
});

