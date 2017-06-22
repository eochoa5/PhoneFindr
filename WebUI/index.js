var express = require('express');
var app = express();
var http = require('http').Server(app);
var io = require('socket.io')(http);

app.get('/', function(req, res){
	res.sendFile(__dirname+'/index.html');

});

app.use(express.static(__dirname));

io.on('connection', function(socket){
	console.log('a user connected: ' + socket.id);

	socket.on('disconnect', function(){
		console.log('a user disconnected: ' + socket.id);
	});

});

http.listen(process.env.PORT || 3000, function(){
	console.log('server running');
});


