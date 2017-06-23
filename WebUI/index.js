var express = require('express');
var app = express();
var http = require('http').Server(app);
var io = require('socket.io')(http);

users = [];
connections = [];

app.get('/', function(req, res){
	res.sendFile(__dirname+'/index.html');

});

app.use(express.static(__dirname));
//drew2g 6/23 ===============================
//push socket to connection array and console log it
//===========================================

io.on('connection', function(socket){
	connections.push(socket);
	console.log('[C] %s sockets connected. New user %s', connections.length, socket.id);

	socket.on('disconnect', function(){
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
	});

});

http.listen(process.env.PORT || 3000, function(){
	console.log('server running');
});

