;
jQuery(function($){
    'use strict';

    var b2Vec2 = Box2D.Common.Math.b2Vec2;
    var b2BodyDef = Box2D.Dynamics.b2BodyDef;
    var b2Body = Box2D.Dynamics.b2Body;
    var b2FixtureDef = Box2D.Dynamics.b2FixtureDef;
    var b2Fixture = Box2D.Dynamics.b2Fixture;
    var b2World = Box2D.Dynamics.b2World;
    var b2MassData = Box2D.Collision.Shapes.b2MassData;
    var b2PolygonShape = Box2D.Collision.Shapes.b2PolygonShape;
    var b2CircleShape = Box2D.Collision.Shapes.b2CircleShape;
    var b2DebugDraw = Box2D.Dynamics.b2DebugDraw;


    /**
     * All the code relevant to Socket.IO is collected in the IO namespace.
     *
     * @type {{init: Function, bindEvents: Function, onConnected: Function, onNewRoomCreated: Function, playerJoinedRoom: Function, beginCountdown: Function, onNewWordData: Function, hostCheckAnswer: Function, gameOver: Function, error: Function}}
     */
    var IO = {

        /**
         * This is called when the page is displayed. It connects the Socket.IO client
         * to the Socket.IO server
         */
        init: function() {
            IO.socket = io.connect();
            IO.bindEvents();
        },

        /**
         * While connected, Socket.IO will listen to the following events emitted
         * by the Socket.IO server, then run the appropriate function.
         */
        bindEvents : function() {
            IO.socket.on('connected', IO.onConnected );
            IO.socket.on('newRoomCreated', IO.onNewRoomCreated );
            IO.socket.on('playerJoinedRoom', IO.playerJoinedRoom );

            IO.socket.on('beginCountdown', IO.beginCountdown );
            IO.socket.on('runMainLoop', IO.runMainLoop);
            IO.socket.on('newPlayerPosition', IO.newPlayerPosition);
            IO.socket.on('hostCheckAnswer', IO.hostCheckAnswer);

            IO.socket.on('gameOver', IO.gameOver);
            IO.socket.on('error', IO.error );
        },

        /**
         * The client is successfully connected!
         */
        onConnected : function() {
            // Cache a copy of the client's socket.IO session ID on the App
            App.mySocketId = IO.socket.socket.sessionid;
            // console.log(data.message);
        },

        /**
         * A new game has been created and a random game ID has been generated.
         * @param data {{ gameId: int, mySocketId: * }}
         */
        onNewRoomCreated : function(data) {
            App.Host.gameInit(data);
        },

        /**
         * A player has successfully joined the game.
         * @param data {{playerName: string, gameId: int, mySocketId: int}}
         */
        playerJoinedRoom : function(data) {
            // When a player joins a room, do the updateWaitingScreen function.
            // There are two versions of this function: one for the 'host' and
            // another for the 'player'.
            //
            // So on the 'host' browser window, the App.Host.updateWaitingScreen function is called.
            // And on the player's browser, App.Player.updateWaitingScreen is called.
            App[App.myRole].updateWaitingScreen(data);
        },

        /**
         * Both players have joined the game.
         * @param data
         */
        beginCountdown : function(data) {
            App[App.myRole].gameCountdown(data);
        },

        // The main game loop
        runMainLoop : function () {
            console.log('Running main loop');
            // Create the canvas
            var $canvas = $("<canvas></canvas>");
            $canvas[0].width = 512;
            $canvas[0].height = 480;
            $canvas[0].id = "gameCanvas";
            App.$gameArea.html($canvas[0]);

            App.stage = new createjs.Stage("gameCanvas");

            App.world = new b2World(new b2Vec2(0, 0) //gravity
                , false); //allow to sleep

            createjs.Ticker.setFPS(App.FPS);
            createjs.Ticker.addEventListener("tick", App.Player.tick);
            createjs.Ticker.useRAF = true;
        },

        newPlayerPosition : function (name, x, y) {
            App.Player.renderPlayers(name, x, y);
        },

        /**
         * A new set of words for the round is returned from the server.
         * @param data
         */
        onNewWordData : function(data) {
            // Update the current round
            App.currentRound = data.round;

            // Change the word for the Host and Player
            App[App.myRole].newWord(data);
        },

        /**
         * A player answered. If this is the host, check the answer.
         * @param data
         */
        hostCheckAnswer : function(data) {
            if(App.myRole === 'Host') {
                App.Host.checkAnswer(data);
            }
        },

        /**
         * Let everyone know the game has ended.
         * @param data
         */
        gameOver : function(data) {
            App[App.myRole].endGame(data);
        },

        /**
         * An error has occurred.
         * @param data
         */
        error : function(data) {
            alert(data.message);
        }

    };

    var App = {

        /**
         * Keep track of the gameId, which is identical to the ID
         * of the Socket.IO Room used for the players and host to communicate
         *
         */
        gameId: 0,

        /**
         * This is used to differentiate between 'Host' and 'Player' browsers.
         */
        myRole: '', // 'Player' or 'Host'

        /**
         * The Socket.IO socket object identifier. This is unique for
         * each player and host. It is generated when the browser initially
         * connects to the server when the page loads for the first time.
         */
        mySocketId: '',

        /**
         * Identifies the current round. Starts at 0 because it corresponds
         * to the array of word data stored on the server.
         */
        currentRound: 0,

        keysDown: {},

        players: [],

        FPS: 30,

        SCALE: 30,

        /* *************************************
         * Setup *
         * *********************************** */

        /**
         * This runs when the page initially loads.
         */
        init: function () {
            App.cacheElements();
            App.showInitScreen();
            App.bindEvents();

            // Initialize the fastclick library
            //FastClick.attach(document.body);

            // Handle keyboard controls
            addEventListener("keydown", function (e) {
                //console.log("keydown");
                App.keysDown[e.keyCode] = true;
            }, false);

            addEventListener("keyup", function (e) {
                //console.log("keyup");
                delete App.keysDown[e.keyCode];
            }, false);

        },

        /**
         * Create references to on-screen elements used throughout the game.
         */
        cacheElements: function () {
            App.$doc = $(document);

            App.world = '';
            App.stage = '';

            // Templates
            App.$gameArea = $('#gameArea');
            App.$templateIntroScreen = $('#intro-screen-template').html();
            App.$templateNewGame = $('#create-game-template').html();
            App.$templateJoinGame = $('#join-game-template').html();
            App.$hostGame = $('#host-game-template').html();
        },

        /**
         * Create some click handlers for the various buttons that appear on-screen.
         */
        bindEvents: function () {
            // Host
            App.$doc.on('click', '#btnCreateGame', App.Host.onCreateClick);

            // Player
            App.$doc.on('click', '#btnJoinGame', App.Player.onJoinClick);
            App.$doc.on('click', '#btnStart',App.Player.onPlayerStartClick);
            App.$doc.on('click', '#btnCloseRoom', App.Player.onCloseRoomClick);
            App.$doc.on('click', '.btnAnswer',App.Player.onPlayerAnswerClick);
            App.$doc.on('click', '#btnPlayerRestart', App.Player.onPlayerRestart);
        },

        /* *************************************
         * Game Logic *
         * *********************************** */

        /**
         * Show the initial Title Screen
         * (with Start and Join buttons)
         */
        showInitScreen: function() {
            App.$gameArea.html(App.$templateIntroScreen);
            App.doTextFit('.title');
        },


        /* *******************************
         * HOST CODE *
         ******************************* */
        Host : {

            /**
             * Contains references to player data
             */
            party : [],

            /**
             * Flag to indicate if a new game is starting.
             * This is used after the first game ends, and players initiate a new game
             * without refreshing the browser windows.
             */
            isNewGame : false,

            /**
             * Keep track of the number of players that have joined the game.
             */
            numPlayersInRoom: 0,

            /**
             * A reference to the correct answer for the current round.
             */
            currentCorrectAnswer: '',

            /**
             * Handler for the "Start" button on the Title Screen.
             */
            onCreateClick: function () {
                // console.log('Clicked "Create A Game"');
                IO.socket.emit('hostCreateNewGame');
            },

            /**
             * The Host screen is displayed for the first time.
             * @param data{{ gameId: int, mySocketId: * }}
             */
            gameInit: function (data) {
                App.gameId = data.gameId;
                App.mySocketId = data.mySocketId;
                App.myRole = 'Host';
                App.Host.numPlayersInRoom = 0;

                App.Host.displayNewGameScreen();
                // console.log("Game started with ID: " + App.gameId + ' by host: ' + App.mySocketId);
            },

            /**
             * Show the Host screen containing the game URL and unique game ID
             */
            displayNewGameScreen : function() {
                // Fill the game screen with the appropriate HTML
                App.$gameArea.html(App.$templateNewGame);

                // Display the URL on screen
                $('#gameURL').text(window.location.href);
                App.doTextFit('#gameURL');

                // Show the gameId / room id on screen
                $('#spanNewGameCode').text(App.gameId);
            },

            /**
             * Update the Host screen when the first player joins
             * @param data{{playerName: string}}
             */
            updateWaitingScreen: function(data) {
                // If this is a restarted game, show the screen.
                if ( App.Host.isNewGame ) {
                    App.Host.displayNewGameScreen();
                }
                // Update host screen
                $('#playersWaiting')
                    .append('<p/>')
                    .text('Player ' + data.playerName + ' joined the game.');

                // Store the new player's data on the Host.
                App.Host.party.push(data);

                // Increment the number of players in the room
                App.Host.numPlayersInRoom += 1;

                // If two players have joined, start the game!
                if (App.Host.numPlayersInRoom === 10) {
                    // console.log('Room is full. Almost ready!');

                    // Let the server know that two players are present.
                    IO.socket.emit('hostRoomFull',App.gameId);
                }
            },

            /**
             * Show the countdown screen
             */
            gameCountdown : function() {

                // Prepare the game screen with new HTML
                App.$gameArea.html(App.$hostGame);
                App.doTextFit('#hostWord');

                // Begin the on-screen countdown timer
                var $secondsLeft = $('#hostWord');
                App.countDown( $secondsLeft, 5, function(){
                    IO.socket.emit('hostCountdownFinished', App.gameId);
                });

                // Display the players' names on screen
                $('#player1Score')
                    .find('.playerName')
                    .html(App.Host.party[0].playerName);

                $('#player2Score')
                    .find('.playerName')
                    .html(App.Host.party[1].playerName);

                // Set the Score section on screen to 0 for each player.
                $('#player1Score').find('.score').attr('id',App.Host.party[0].mySocketId);
                $('#player2Score').find('.score').attr('id',App.Host.party[1].mySocketId);
            },

            /**
             * Show the word for the current round on screen.
             * @param data{{round: *, word: *, answer: *, list: Array}}
             */
            newWord : function(data) {
                // Insert the new word into the DOM
                $('#hostWord').text(data.word);
                App.doTextFit('#hostWord');

                // Update the data for the current round
                App.Host.currentCorrectAnswer = data.answer;
                App.Host.currentRound = data.round;
            },

            /**
             * Check the answer clicked by a player.
             * @param data{{round: *, playerId: *, answer: *, gameId: *}}
             */
            checkAnswer : function(data) {
                // Verify that the answer clicked is from the current round.
                // This prevents a 'late entry' from a player whos screen has not
                // yet updated to the current round.
                if (data.round === App.currentRound){

                    // Get the player's score
                    var $pScore = $('#' + data.playerId);

                    // Advance player's score if it is correct
                    if( App.Host.currentCorrectAnswer === data.answer ) {
                        // Add 5 to the player's score
                        $pScore.text( +$pScore.text() + 5 );

                        // Advance the round
                        App.currentRound += 1;

                        // Prepare data to send to the server
                        var data = {
                            gameId : App.gameId,
                            round : App.currentRound
                        }

                        // Notify the server to start the next round.
                        IO.socket.emit('hostNextRound',data);

                    } else {
                        // A wrong answer was submitted, so decrement the player's score.
                        $pScore.text( +$pScore.text() - 3 );
                    }
                }
            },


            /**
             * All 10 rounds have played out. End the game.
             * @param data
             */
            endGame : function(data) {
                // Get the data for player 1 from the host screen
                var $p1 = $('#player1Score');
                var p1Score = +$p1.find('.score').text();
                var p1Name = $p1.find('.playerName').text();

                // Get the data for player 2 from the host screen
                var $p2 = $('#player2Score');
                var p2Score = +$p2.find('.score').text();
                var p2Name = $p2.find('.playerName').text();

                // Find the winner based on the scores
                var winner = (p1Score < p2Score) ? p2Name : p1Name;
                var tie = (p1Score === p2Score);

                // Display the winner (or tie game message)
                if(tie){
                    $('#hostWord').text("It's a Tie!");
                } else {
                    $('#hostWord').text( winner + ' Wins!!' );
                }
                App.doTextFit('#hostWord');

                // Reset game data
                App.Host.numPlayersInRoom = 0;
                App.Host.isNewGame = true;
            },

            /**
             * A player hit the 'Start Again' button after the end of a game.
             */
            restartGame : function() {
                App.$gameArea.html(App.$templateNewGame);
                $('#spanNewGameCode').text(App.gameId);
            }
        },


        /* *****************************
         * PLAYER CODE *
         ***************************** */

        Player : {
            /**
             * A reference to the socket ID of the Host
             */
            hostSocketId: '',

            /**
             * The player's name entered on the 'Join' screen.
             */
            myName: '',

            x: 0,
            y: 0,
            speed: 150,

            /**
             * Click handler for the 'JOIN' button
             */
            onJoinClick: function () {
                console.log('Clicked "Join A Game"');

                // Display the Join Game HTML on the player's screen.
                App.$gameArea.html(App.$templateJoinGame);
            },

            /**
             * The player entered their name and gameId (hopefully)
             * and clicked Start.
             */
            onPlayerStartClick: function() {
                console.log('Player clicked "Start"');

                // collect data to send to the server
                var data = {
                    gameId : +($('#inputGameId').val()),
                    playerName : $('#inputPlayerName').val() || 'anon'
                };

                // Send the gameId and playerName to the server
                IO.socket.emit('playerJoinGame', data);

                // Set the appropriate properties for the current player.
                App.myRole = 'Player';
                App.Player.myName = data.playerName;
            },

            onCloseRoomClick: function() {
                console.log('Host clicked "Start"');

                // Let the server know that host decided to start.
                IO.socket.emit('hostRoomFull',App.gameId);
            },

            /**
             * Click handler for the Player hitting a word in the word list.
             */
            onPlayerAnswerClick: function() {
                // console.log('Clicked Answer Button');
                var $btn = $(this); // the tapped button
                var answer = $btn.val(); // The tapped word

                // Send the player info and tapped word to the server so
                // the host can check the answer.
                var data = {
                    gameId: App.gameId,
                    playerId: App.mySocketId,
                    answer: answer,
                    round: App.currentRound
                }
                IO.socket.emit('playerAnswer',data);
            },

            /**
             * Click handler for the "Start Again" button that appears
             * when a game is over.
             */
            onPlayerRestart : function() {
                var data = {
                    gameId : App.gameId,
                    playerName : App.Player.myName
                }
                IO.socket.emit('playerRestart',data);
                App.currentRound = 0;
                $('#gameArea').html("<h3>Waiting on host to start new game.</h3>");
            },

            /**
             * Display the waiting screen for player 1
             * @param data
             */
            updateWaitingScreen : function(data) {
                if(IO.socket.socket.sessionid === data.mySocketId){
                    App.myRole = 'Player';
                    App.gameId = data.gameId;

                    $('#playerWaitingMessage')
                        .append('<p/>')
                        .text('Joined Game ' + data.gameId + '. Please wait for game to begin.');
                }
            },

            /**
             * Display 'Get Ready' while the countdown timer ticks down.
             * @param hostData
             */
            gameCountdown : function(hostData) {
                App.Player.hostSocketId = hostData.mySocketId;
                $('#gameArea')
                    .html('<div class="gameOver">Get Ready!</div>');
            },

            /**
             * Show the list of words for the current round.
             * @param data{{round: *, word: *, answer: *, list: Array}}
             */
            newWord : function(data) {
                // Create an unordered list element
                var $list = $('<ul/>').attr('id','ulAnswers');

                // Insert a list item for each word in the word list
                // received from the server.
                $.each(data.list, function(){
                    $list // <ul> </ul>
                        .append( $('<li/>') // <ul> <li> </li> </ul>
                            .append( $('<button/>') // <ul> <li> <button> </button> </li> </ul>
                                .addClass('btnAnswer') // <ul> <li> <button class='btnAnswer'> </button> </li> </ul>
                                .addClass('btn') // <ul> <li> <button class='btnAnswer'> </button> </li> </ul>
                                .val(this) // <ul> <li> <button class='btnAnswer' value='word'> </button> </li> </ul>
                                .html(this) // <ul> <li> <button class='btnAnswer' value='word'>word</button> </li> </ul>
                            )
                        )
                });

                // Insert the list onto the screen.
                $('#gameArea').html($list);
            },


            tick : function(evt) {
                App.Player.moveSelf(evt.delta / 1000);
                for(var name in App.players) {
                    App.players[name].skin.x = App.players[name].body.GetPosition().x * App.SCALE;
                    App.players[name].skin.y = App.players[name].body.GetPosition().y * App.SCALE;
                }
                App.world.Step(evt.delta / 1000, 10, 10);
                App.world.DrawDebugData();
                App.stage.update();
            },


        // Update player object
            moveSelf : function(modifier) {
                var initx = App.Player.x;
                var inity = App.Player.y;

                if (38 in App.keysDown) { // Player holding up
                    App.Player.y -= App.Player.speed * modifier;
                }
                if (40 in App.keysDown) { // Player holding down
                    App.Player.y += App.Player.speed * modifier;
                }
                if (37 in App.keysDown) { // Player holding left
                    App.Player.x -= App.Player.speed * modifier;
                }
                if (39 in App.keysDown) { // Player holding right
                    App.Player.x += App.Player.speed * modifier;
                }

                if(initx != App.Player.x || inity != App.Player.y){
                    //console.log("move in game: " + App.gameId);
                    IO.socket.emit('playerMove', App.gameId, App.Player.myName, App.Player.x, App.Player.y);
                }
            },

            renderPlayers : function (name, x, y) {
                console.log('Opponent: ' + name + ' moved to: x:' + x + ' y:' + y);

                if(App.players[name] == undefined){
                    var diameter = 20;
                    App.players[name] = {};

                    //easeljs
                    var skin = new createjs.Shape();
                    if(name === App.Player.myName){
                        skin.graphics.beginFill("blue").drawCircle(0, 0, diameter);
                    }
                    else {
                        skin.graphics.beginFill("red").drawCircle(0, 0, diameter);
                    }
                    skin.x = 0;
                    skin.y = 0;
                    App.stage.addChild(skin);

                    //box2d
                    var fixDef = new b2FixtureDef;
                    fixDef.density = 1.0;
                    fixDef.friction = 0.5;
                    fixDef.restitution = 0.2;
                    fixDef.shape = new b2CircleShape(diameter * 2 / App.SCALE);

                    var bodyDef = new b2BodyDef;
                    bodyDef.type = b2Body.b2_dynamicBody;
                    // positions the center of the object (not upper left!)
                    bodyDef.position.x = diameter/2 / App.SCALE;
                    bodyDef.position.y = diameter/2 / App.SCALE;

                    var body = App.world.CreateBody(bodyDef);
                    body.CreateFixture(fixDef);

                    var actor = new function(body, skin) {
                        this.body = body;
                        this.skin = skin;
                        this.update = function() { // translate box2d positions to pixels
                            this.skin.x = this.body.GetPosition().x * App.SCALE;
                            this.skin.y = this.body.GetPosition().y * App.SCALE;
                        }
                    };

                    body.SetUserData(actor);  // set actor as userdata of body so we can get at it later if we need to


                    // debug draw:
                    var debugDraw = new b2DebugDraw;
                    debugDraw.SetSprite(App.stage.canvas.getContext("2d"));
                    debugDraw.SetDrawScale(App.SCALE);
                    debugDraw.SetFlags(b2DebugDraw.e_shapeBit | b2DebugDraw.e_jointBit);
                    App.world.SetDebugDraw(debugDraw);

                    App.players[name].skin = skin;
                    App.players[name].body = body;
                    App.players[name].actor = actor;
                }

                App.players[name].body.SetPositionAndAngle(new b2Vec2(x / App.SCALE,y / App.SCALE));
                //App.players[name].body.GetUserData().skin.update();
            },


        /**
             * Show the "Game Over" screen.
             */
            endGame : function() {
                $('#gameArea')
                    .html('<div class="gameOver">Game Over!</div>')
                    .append(
                        // Create a button to start a new game.
                        $('<button>Start Again</button>')
                            .attr('id','btnPlayerRestart')
                            .addClass('btn')
                            .addClass('btnGameOver')
                    );
            }
        },


        /* **************************
         UTILITY CODE
         ************************** */

        /**
         * Display the countdown timer on the Host screen
         *
         * @param $el The container element for the countdown timer
         * @param startTime
         * @param callback The function to call when the timer ends.
         */
        countDown : function( $el, startTime, callback) {

            // Display the starting time on the screen.
            $el.text(startTime);
            App.doTextFit('#hostWord');

            // console.log('Starting Countdown...');

            // Start a 1 second timer
            var timer = setInterval(countItDown,1000);

            // Decrement the displayed timer value on each 'tick'
            function countItDown(){
                startTime -= 1
                $el.text(startTime);
                App.doTextFit('#hostWord');

                if( startTime <= 0 ){
                    // console.log('Countdown Finished.');

                    // Stop the timer and do the callback.
                    clearInterval(timer);
                    callback();
                    return;
                }
            }

        },

        /**
         * Make the text inside the given element as big as possible
         * See: https://github.com/STRML/textFit
         *
         * @param el The parent element of some text
         */
        doTextFit : function(el) {
            textFit(
                $(el)[0],
                {
                    alignHoriz:true,
                    alignVert:false,
                    widthOnly:true,
                    reProcess:true,
                    maxFontSize:300
                }
            );
        }

    };

    IO.init();
    App.init();

}($));