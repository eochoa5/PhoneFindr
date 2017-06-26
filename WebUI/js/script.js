
  // Initialize Firebase
  var config = {
    apiKey: "AIzaSyA_yDgR6Ru2hVlCPT86y1FKH2Jx2HI0PGc",
    authDomain: "phonefindr-39f72.firebaseapp.com",
    databaseURL: "https://phonefindr-39f72.firebaseio.com",
    projectId: "phonefindr-39f72",
    storageBucket: "phonefindr-39f72.appspot.com",
    messagingSenderId: "753747182589"
  };
  firebase.initializeApp(config);

  firebase.auth().onAuthStateChanged(function(user) {
  
  if (user) {
    //redirect to dashboard
    window.location.href = "/dashboard";
   
  } 

});


$('.form-control').keypress(function(){
     $('.log-status').removeClass('wrong-entry');
 });

  function signIn(target){
    $(target).prop("disabled",true);

    var email = $('#email').val();
    var pass = $('#password').val();
  
      
      firebase.auth().signInWithEmailAndPassword(email, pass).catch(function(error) {
  
      var errorCode = error.code;
      var errorMessage = error.message;
     
       $('.log-status').addClass('wrong-entry');
       $('.alert').fadeIn(500);

       setTimeout(function() { 
         $('.alert').fadeOut(1000);
         $(target).prop("disabled",false);

        }, 700);
        
  
     });
  }



  

