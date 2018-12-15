const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendMessageNotification = functions.database.ref('messages/{user_id1}/{user_id2}/{message_id}').onCreate((snapshot, context) =>{
    const message_id = context.params.message_id;
    const user_id1 = context.params.user_id1;
    const user_id2 = context.params.user_id2;
    const message = snapshot.val();
    const sender = message.sender;

    if(user_id2 == sender){
        return console.log('Not the sender');
    }

    const deviceToken = admin.database().ref(`/users/${user_id2}/deviceToken`).once('value');
    return deviceToken.then(result =>{
        const device_token = result.val();

        const displayName = admin.database().ref(`/users/${user_id2}/displayName`).once('value');
        return displayName.then(result =>{

            const sender_name = result.val();
            var message_content = message.content;
            const message_type = message.type;

            if(message_type == 'image'){
                message_content = 'New Image Message';
            }else if(message_type == 'math'){
                message_content = 'New Math Expression';
            }else if(message_type == 'location'){
                message_content = 'New Location Message';
            }

            const payload = {
                token : device_token,
                android:{
                    notification:{
                        title : sender_name,
                        body : message_content,
                        tag : user_id1
                    }
                }
            };

            return admin.messaging().send(payload).then(response =>{
                console.log("Notification sent to " + user_id1 + " device token is " + device_token);
            });

        });

    });
});

exports.sendRequestNotification = functions.database.ref('requests/{user_id1}/{user_id2}').onCreate((snapshot, context) =>{
    const user_id1 = context.params.user_id1;
    const user_id2 = context.params.user_id2;
    const request = snapshot.val();
    const request_type = request.type;

    if(request_type == 'sent'){
        return console.log('This user sent the request');
    }

    const deviceToken = admin.database().ref(`/users/${user_id1}/deviceToken`).once('value');
    return deviceToken.then(result =>{
        const device_token = result.val();

        const displayName = admin.database().ref(`/users/${user_id2}/displayName`).once('value');
        return displayName.then(result =>{
            const sender_name = result.val();

            const payload = {
                token : device_token,
                android:{
                    notification:{
                        title : sender_name,
                        body : 'New Message Request',
                        tag : user_id2
                    }
                }
            };

            return admin.messaging().send(payload).then(response =>{
                console.log("Notification sent to " + user_id1 + " device token is " + device_token);
            });
        });
    });

});
