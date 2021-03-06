CREATE EXTENSION IF NOT EXISTS "uuid-ossp";         -- Permite usar identificadores UUID

/* ********************* DROP TABLES ********************* */
DROP TABLE IF EXISTS FriendshipStatus;
DROP TABLE IF EXISTS FriendshipStatusCode;
DROP TABLE IF EXISTS Friendship;
DROP TABLE IF EXISTS UserGroup;
DROP TABLE IF EXISTS Assistance;
DROP TABLE IF EXISTS AssistanceStatus;
DROP TABLE IF EXISTS AssistanceStatusCode;
DROP TABLE IF EXISTS Meeting;
DROP TABLE IF EXISTS Place;
DROP TABLE IF EXISTS PlaceAddress;
DROP TABLE IF EXISTS UserAddress;
DROP TABLE IF EXISTS UserProfile;

/* ******************** CREATE TABLES ******************** */
/* *************** ACCOUNTS *************** */
CREATE TABLE UserProfile (
    userProfileID UUID          DEFAULT uuid_generate_v4(),
    firstName     VARCHAR(30)   NOT NULL,
    surname1      VARCHAR(50),
    surname2      VARCHAR(50),
    email         VARCHAR(100)  NOT NULL,
    birthDate     DATE          NOT NULL,
    joinDate      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    description   VARCHAR,
    nickName      VARCHAR(50)   NOT NULL,
    password      VARCHAR       NOT NULL,
    avatarPath    VARCHAR,                  -- Path to user profile picture
    score         FLOAT         NOT NULL DEFAULT 0.0,
    role          SMALLINT      NOT NULL,
    userAddressID UUID,

    CONSTRAINT PK_UserProfile PRIMARY KEY (userProfileID),
    CONSTRAINT UNIQUE_UserProfile_email UNIQUE (email),
    CONSTRAINT UNIQUE_UserProfile_nickName UNIQUE (nickName)
);

CREATE TABLE UserAddress (
    userAddressID   UUID            DEFAULT uuid_generate_v4(),
    city            VARCHAR(50),
    region          VARCHAR(50),
    postalCode      VARCHAR(10),
    country         VARCHAR(50),
    userProfileID   UUID,

    CONSTRAINT PK_UserAddress PRIMARY KEY (userAddressID),
    CONSTRAINT FK_UserAddress_TO_UserProfile FOREIGN KEY (userProfileID)
        REFERENCES UserProfile(userProfileID)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE UserGroup (
    groupID     UUID            DEFAULT uuid_generate_v4(),
    name        VARCHAR(20)     NOT NULL,

    CONSTRAINT PK_Group PRIMARY KEY (groupID)
);

CREATE TABLE Friendship (
    requesterID     UUID,
    targetID        UUID,
    groupID         UUID,

    CONSTRAINT PK_Friendship PRIMARY KEY (requesterID, targetID),
    CONSTRAINT FK_Friendship_TO_UserGroup FOREIGN KEY (groupID)
        REFERENCES UserGroup(groupID)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    CONSTRAINT FK_Friendship_TO_UserProfile_Requester FOREIGN KEY (requesterID)
        REFERENCES UserProfile(userProfileID)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT FK_Friendship_TO_UserProfile_Target FOREIGN KEY (targetID)
        REFERENCES UserProfile(userProfileID)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE FriendshipStatusCode (
    statusID    CHAR,
    name        VARCHAR(20)     NOT NULL,

    CONSTRAINT PK_FriendshipStatusCode PRIMARY KEY (statusID),
    CONSTRAINT UNIQUE_FriendshipStatusCode_Name UNIQUE (name)
);

CREATE TABLE FriendshipStatus (
    requesterID     UUID,
    targetID        UUID,
    lastUpdated     TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    specifierID     UUID            NOT NULL,
    statusID        CHAR            NOT NULL,

    CONSTRAINT PK_FriendshipStatus PRIMARY KEY (requesterID, targetID, lastUpdated),
    CONSTRAINT FK_FriendshipStatus_TO_FriendshipStatusCode FOREIGN KEY (statusID)
        REFERENCES FriendshipStatusCode(statusID)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    CONSTRAINT FK_Friendship_TO_UserProfile_Requester FOREIGN KEY (requesterID)
        REFERENCES UserProfile(userProfileID)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT FK_Friendship_TO_UserProfile_Target FOREIGN KEY (targetID)
        REFERENCES UserProfile(userProfileID)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT FK_Friendship_TO_UserProfile_Specifier FOREIGN KEY (specifierID)
        REFERENCES UserProfile(userProfileID)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);


/* *************** EVENTS *************** */
CREATE TABLE PlaceAddress (
    placeAddressID  UUID            DEFAULT uuid_generate_v4(),
    street          VARCHAR(50),
    city            VARCHAR(50),
    number          smallint,
    postalCode      VARCHAR(10),
    region          VARCHAR(50),
    country         VARCHAR(50),
    imagePath       VARCHAR,        -- Path to place address picture

    CONSTRAINT PK_PlaceAddress PRIMARY KEY (placeAddressID),
    CONSTRAINT CHECK_PlaceAddress_NumberIsPositive CHECK (number > 0)
);

CREATE TABLE Place (
    placeID         UUID        DEFAULT uuid_generate_v4(),
    imagePath       VARCHAR,        -- Path to place picture
    placeAddressID  UUID,

    CONSTRAINT PK_Place PRIMARY KEY (placeID),
    CONSTRAINT FK_Place_TO_PlaceAddress FOREIGN KEY (placeAddressID)
        REFERENCES PlaceAddress(placeAddressID)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);

CREATE TABLE Meeting (
    meetingID       UUID,
    startDate       TIMESTAMP   NOT NULL,
    endDate         TIMESTAMP,
    creationDate    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    description     TEXT,
    maxAssistents   INTEGER,
    rating          FLOAT       NOT NULL DEFAULT 0.0,
    creatorID       UUID        NOT NULL,
    placeID         UUID,

    CONSTRAINT PK_Meeting PRIMARY KEY (meetingID),
    CONSTRAINT FK_Meeting_TO_UserProfile FOREIGN KEY (creatorID)
        REFERENCES UserProfile(userProfileID)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    CONSTRAINT FK_Meeting_TO_Place FOREIGN KEY (placeID)
        REFERENCES Place(placeID)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);

CREATE TABLE Assistance (
    invitingUserID      UUID,
    invitedUserID       UUID,
    meetingID           UUID    NOT NULL,
    inviteDate          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT PK_Assistance PRIMARY KEY (invitingUserID, invitedUserID, meetingID),
    CONSTRAINT FK_Assistance_TO_UserProfile_Inviting FOREIGN KEY (invitingUserID)
        REFERENCES UserProfile(userProfileID)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT FK_Assistance_TO_UserProfile_Invited FOREIGN KEY (invitingUserID)
        REFERENCES UserProfile(userProfileID)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT FK_Assistance_TO_Meeting FOREIGN KEY (meetingID)
        REFERENCES Meeting(meetingID)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE AssistanceStatusCode (
    statusID    CHAR,
    name        VARCHAR(20)     NOT NULL,

    CONSTRAINT PK_AssistanceStatusCode PRIMARY KEY (statusID),
    CONSTRAINT UNIQUE_AssistanceStatusCode_Name UNIQUE (name)
);

CREATE TABLE AssistanceStatus (
    invitingUserID       UUID,
    invitedUserID        UUID,
    lastUpdated          TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    statusID             CHAR            NOT NULL,

    CONSTRAINT PK_AssistanceStatus PRIMARY KEY (invitingUserID, invitedUserID, lastUpdated),
    CONSTRAINT FK_AssistanceStatus_TO_FAssistanceStatusCode FOREIGN KEY (statusID)
        REFERENCES AssistanceStatusCode(statusID)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    CONSTRAINT FK_Friendship_TO_UserProfile_Inviting FOREIGN KEY (invitingUserID)
        REFERENCES UserProfile(userProfileID)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT FK_Friendship_TO_UserProfile_Invited FOREIGN KEY (invitedUserID)
        REFERENCES UserProfile(userProfileID)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);


/* *************** MESSAGES *************** */
/*
CREATE TABLE MessageStatusCode (
    statusID    CHAR,
    name        VARCHAR(20)     NOT NULL,

    CONSTRAINT PK_MessageStatusCode PRIMARY KEY (statusID),
    CONSTRAINT UNIQUE_MessageStatusCode_Name UNIQUE (name)
);

CREATE TABLE PrivateMessage (
    messageID       UUID,
    authorID        UUID    NOT NULL,
    creationDate    TIMESTAMP   NOT NULL,
    text            TEXT    NOT NULL,
    conversationID  UUID    NOT NULL,
) */
