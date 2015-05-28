/**
 * File contains code for the LoginCredential database
 * that allows user-information manipulation and storage.
 */

var StringConst = require('./string_const').StringConst;
var postgresDB = require('pg'); // postgres database module
var client = new postgresDB.Client(StringConst.DB_CONNECTION_STRING);
var UserClass = require('./user');

var dbName = StringConst.LOGIN_DB;

/**
 * Returns object that allows manipulation of LoginCredential database.
 *
 * @param tableName specifies if table or test-table will be used (separate to avoid data corruption during testing)
 */
exports.LoginCredentials = function (tableName) {

    dbName = tableName; // set dbName (may be table or test-table name)

    /**
     * Function invocation connects to DB
     */
    (function connectClient () {
        client.connect(function(err) {
            if(err) {
                return console.error('could not connect to postgres', err);
            }
        });
    })();

    return {

        /**
         * Method inserts new user.
         * 
         * @param userID of new user
         * @param password of new user
         * @param email of new user
         * @param entityType of new user
         * @param insCallback testing callback: rowCount is returned and checked against expected value
         * @return boolean - true if valid query, false otherwise
         */
        insertNewUser: function(userID, password, email, entityType, insCallback) {

            try {
                if (!userID || !password  || !email || !entityType || !insCallback) {
                    return false;
                } else {

                    if (entityType !== StringConst.DOCTOR_ENTITY && entityType !== StringConst.PATIENT_ENTITY) {
                        console.log("!! Error in user insertion: entity is of invalid type \'" + entityType + "\' .");

                        return false;
                    }

                    client.query("INSERT INTO " + dbName + "(" + StringConst.KEY_USER_ID + ", "
                            + StringConst.KEY_EMAIL + ", " + StringConst.KEY_ENTITY_TYPE + ", "
                            + StringConst.KEY_PASSWORD + ") values($1, $2, $3, $4)",
                            [userID, email, entityType, password],

                        function(err, result) {
                            if (err) {

                                insCallback(0);  // error occurred - 0 rows modified; return
                            } else {

                                insCallback(result.rowCount);
                            }
                        });

                    return true;
                }
            } catch (err) {
                console.log("!!Error in LoginCredentials.insertNewUser(): " + err);
                return false;
            }
        },

        /**
         * Method returns user if found in DB.
         * 
         * @param userID of requested user
         * @param getCallback testing callback: rowCount is returned and checked against expected value
         * @return boolean - true if valid query, false otherwise
         */
        getUser: function(userID, getCallback) {

            try {
                if (!userID === undefined || !getCallback) {
                    return false;
                } else {
                    client.query( "SELECT * FROM " + dbName + " WHERE " + StringConst.KEY_USER_ID + " = \'"
                            + userID + "\'",

                        function(err, result) {
                            if (err) {

                                insCallback(0);  // error occurred - 0 rows modified; return
                            } else {

                                if (result.rowCount > 0) {
                                    var queriedRow = UserClass.User();

                                    queriedRow.setUserID(result.rows[0]._userid);
                                    queriedRow.setEntityType(result.rows[0].entitytype);
                                    queriedRow.setPassword(result.rows[0].password);
                                    queriedRow.setEmail(result.rows[0].email);

                                    getCallback(result.rowCount, queriedRow);
                                } else {

                                    getCallback(0, null);
                                }
                            }
                        });

                    return true;
                }
            } catch (err) {
                console.log("!!Error in LoginCredentials.getUser(): " + err);
                return false;
            }
        },

        /**
         * Method updates a users password, email, and entityType
         * 
         * @param userID used to find user
         * @param password used to update user information
         * @param email used to update user information
         * @param entityType used to update user information
         * @param updateCallback testing callback: rowCount is returned and checked against expected value
         * @return boolean - true if valid query, false otherwise
         */
        updateUser: function(userID, password, email, entityType, updateCallback) {

            try {
                if (!userID || !password || !email || !entityType || !updateCallback) {
                    return false;
                } else {

                    if (entityType !== StringConst.DOCTOR_ENTITY && entityType !== StringConst.PATIENT_ENTITY) {
                        console.log("!! Error in user update: entity is of invalid type \'" + entityType + "\' .");

                        return false;
                    }

                    client.query( "UPDATE " + dbName + " SET " + StringConst.KEY_EMAIL + " = \'"
                        + email + "\', " + StringConst.KEY_PASSWORD + " = \'" + password + "\', "
                        + StringConst.KEY_ENTITY_TYPE + " = \'" + entityType + "\' WHERE "
                        + StringConst.KEY_USER_ID + " = \'" + userID + "\'",

                        function(err, result) {
                            if (err) {

                                updateCallback(0);  // error occurred - 0 rows modified; return
                            } else {

                                updateCallback(result.rowCount);
                            }
                        });

                    return true;
                }
            } catch (err) {
                console.log("!!Error in LoginCredentials.updateUser(): " + err);
                return false;
            }
        },

        /**
         * Method deletes user from DB
         *
         * @param userID used to find and delete user
         * @param delCallback testing callback: rowCount is returned and checked against expected value
         * @return boolean - true if valid query, false otherwise
         */
        deleteUser: function(userID, delCallback) {

            try {
                if (!userID || !delCallback) {
                    return false;
                } else {
                    client.query( "DELETE FROM " + dbName + " WHERE " + StringConst.KEY_USER_ID
                            + " = \'" + userID + "\'",

                        function(err, result) {
                            if (err) {

                                delCallback(0);  // error occurred - 0 rows modified; return
                            } else {

                                delCallback(result.rowCount);
                            }
                        });

                    return true;
                }
            } catch (err) {
                console.log("!!Error in LoginCredentials.deleteUser(): " + err);
                return false;
            }
        }
    }
};