/** 
 * File contains code for creating 
 * NDN-compliant data packets
 **/

var NameField = require('./namefield');
var StringConst = require('./string_const').StringConst;
var Utils = require('./utils').Utils;

/**
 * Enables creation of an NDN-Compliant Data packet in the form of a string.
 *
 * Each component of a packet has its own creation method.
 *
 * Specification found here: http://named-data.net/doc/ndn-tlv/data.html
 */
exports.DataPacket = function () {
    return {

        /**
         * Constructor allows specified freshnessPeriod, signatureType,
         * and contentType - all params that user may tune for their benefit.
         *
         * @param userDataID specifies data producer
         * @param sensorID specifies health-sensor type (e.g., heart sensor)
         * @param timeString specifies when packet was created
         * @param processID specifies what process should be invoked upon reception (e.g., store in cache)
         * @param content packet content payload
         * @param contentType type of content (see NDN specification)
         * @param freshnessPeriod time until data "expires" (see NDN specification)
         * @param signatureType type of signature used on packet (see NDN specification)
         */
        DataPacket: function (userDataID, sensorID, timestring, processID, content, 
                    contentType, freshnessPeriod, signatureType) {
            
             // if current time requested, provide it
            if (timestring === StringConst.CURRENT_TIME) {
                console.log("if");
                timestring = Utils.getCurrentTime();
            } else {
                console.log("else");
            }

            this.nameField = NameField.NameField();
            this.nameField.NameField(userDataID, sensorID, timestring, processID);
            this.content = content;
            this.contentType = contentType;
            this.freshnessPeriod= freshnessPeriod;
            this.SignatureType = signatureType;

            // 3 content types are currently defined
            this.CONTENT_TYPE_DEFAULT = 0;
            this.CONTENT_TYPE_LINK = 1;
            this.CONTENT_TYPE_KEY = 2;

            // 3 types of signatures are currently defined
            this.SIGNATURE_DIGEST_SHA = 0;
            this.SIGNATURE_SHA_WITH_RSA = 1;
            this.SIGNATURE_SHA_WITH_ECDSA = 3;

            this.DEFAULT_FRESH = 100000; // arbitrarily chosen

            // perform input validation on content type
            if (contentType === this.CONTENT_TYPE_KEY) {
                this.contentType = this.CONTENT_TYPE_KEY;
            } else if (contentType === this.CONTENT_TYPE_LINK) {
                this.contentType = this.CONTENT_TYPE_LINK;
            } else {
                this.contentType = this.CONTENT_TYPE_DEFAULT; // if no others match, assign default
            }

            // perform input validation on signature type
            if (signatureType === this.SIGNATURE_SHA_WITH_ECDSA) {
                this.signatureType = this.SIGNATURE_SHA_WITH_ECDSA;
            } else if (signatureType === this.SIGNATURE_SHA_WITH_RSA) {
                this.signatureType = this.SIGNATURE_SHA_WITH_RSA;
            } else {
                this.signatureType = this.SIGNATURE_DIGEST_SHA; // if no others match, assign digest sha
            }

            // perform input validation on freshness period
            if (freshnessPeriod >= 0) {
                this.freshnessPeriod = freshnessPeriod;
            } else {
                this.freshnessPeriod = 0; // negatives aren't possible; assign 0
            }
        },

        /**
         * MetaInfo ::=
         * --------------
         * META-INFO-TYPE TLV-LENGTH
         * ContentType?
         * FreshnessPeriod?
         * FinalBlockId?
         * --------------
         *
         * @return MetaInfo as definition above shows (see NDN specification)
         */
        createMetaInfo: function () {
            var content = this.createContentType();
            content += " " + this.createFreshnessPeriod()
            content += " " + this.createFinalBlockId()

            return "META-INFO-TYPE 0 " + (content.length).toString() + " " + content;
        },

        /**
         * ContentType ::=
         * --------------
         * CONTENT-TYPE-TYPE TLV-LENGTH
         * nonNegativeInteger
         * --------------
         *
         * @return ContentType as definition above shows (see NDN specification)
         **/
        createContentType: function () {
            var content = (this.contentType).toString();
            return "CONTENT-TYPE-TYPE " + (content.length).toString() + " " + content;
        },

        /**
         * FreshnessPeriod ::=
         * --------------
         * FRESHNESS-PERIOD-TLV TLV-LENGTH
         * nonNegativeInteger
         * --------------
         *
         * @return FreshnessPeriod as definition above shows (see NDN specification)
         **/
        createFreshnessPeriod: function () {

            /** the integer here specifies
             *  "how long a node should wait after the arrival of this data before marking it stale"
             *
             *  NOTE: value is in milliseconds
             *
             *  NOTE: stale data is still valid data; stale just means producer may have updated data
             *  **/

            var content = (this.freshnessPeriod).toString(); 

            return "FRESHNESS-PERIOD-TLV " + (content.length).toString() + " " + content;
        },

        /**
         * FinalBlockId ::=
         * --------------
         * FINAL-BLOCK-ID-TLV TLV-LENGTH
         * NameComponent
         * --------------
         *
         * @return FinalBlockId as definition above shows (see NDN specification)
         **/
        createFinalBlockId: function () {
            var content = this.nameField.createNameComponent();

            return "FINAL-BLOCK-ID-TLV " + (content.length).toString() + " " + content;
        },

        /**
         * Content ::=
         * --------------
         * CONTENT-TYPE TLV-LENGTH Byte*
         * --------------
         *
         * @return Content as definition above shows (see NDN specification)
         **/
        createContent: function () {
            var content = this.content; 
            return "CONTENT-TYPE " + (content.length).toString() + " " + content;
        },

        /**
         * Signature ::==
         * --------------
         * SignatureInfo SignatureBits
         * --------------
         *
         * @return Signature as definition above shows (see NDN specification)
         **/
        createSignature: function () {
            var signatureBits = "null"; // TODO - rework
            return this.createSignatureInfo() + " " + signatureBits;
        },

        /**
         * SignatureInfo ::==
         * --------------
         * SIGNATURE-INFO-TYPE TLV-LENGTH
         * SignatureType
         * KeyLocator?
         * ... (SignatureType-specific TLVs)
         * --------------
         *
         * @return SignatureInfo as definition above shows (see NDN specification)
         **/
        createSignatureInfo: function () {
            var content = this.createSignatureType();
            content += " " + this.createKeyLocator()
            return "SIGNATURE-INFO-TYPE " + (content.length).toString() + " " + content;
        },

        /**
         * KeyLocator ::=
         * --------------
         * KEY-LOCATOR-TYPE TLV-LENGTH (Name | KeyDigest)
         * --------------
         *
         * @return KeyLocator as definition above shows (see NDN specification)
         */
         createKeyLocator: function() {
            var content = "null"; // TODO - rework
            return "KEY-LOCATOR-TYPE " + (content.length).toString() + " " + content;
        },

        /**
         * SignatureType ::==
         * --------------
         * SIGNATURE-TYPE-TYPE TLV-LENGTH
         * nonNegativeInteger
         * --------------
         *
         * @return SignatureType as definition above shows (see NDN specification)
         **/
        createSignatureType: function () {
            var content = (this.signatureType).toString(); 
            return "SIGNATURE-TYPE-TYPE " + (content.length).toString() + " " + content;
        },

        /**
         * DATA ::=
         * --------------
         * DATA-TLV TLV-LENGTH
         * Name
         * MetaInfo?
         * Content
         * Signature
         * --------------
         *
         * @return Data packet as definition above shows (see NDN specification)
         **/
        createDATA: function () {
            var content = this.nameField.createName();
            content += " " + this.createMetaInfo();
            content += " " + this.createContent();
            content += " " + this.createSignature();

            return "DATA-TLV " + (content.length).toString() + " " + content;
        },

        /**
         * Method returns NDN-compliant Data string
         */
    	toString: function () {
            return this.createDATA();
        }
    };
}