CREATE EXTENSION IF NOT EXISTS "uuid-ossp";         -- Permite usar identificadores UUID

/* ********************* DROP TABLES ********************* */
DROP TABLE IF EXISTS FriendshipStatus;
DROP TABLE IF EXISTS Friendship;
DROP TABLE IF EXISTS GroupTable;
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
    userID        UUID          DEFAULT uuid_generate_v1(),
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
    role          VARCHAR       NOT NULL,
    userAddressID UUID,

    CONSTRAINT PK_UserProfile PRIMARY KEY (userID),
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
        REFERENCES UserProfile(userID)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE GroupTable (
    groupID     UUID            DEFAULT uuid_generate_v4(),
    name        VARCHAR(20)     NOT NULL,
    creatorID   uuid            NOT NULL,

    CONSTRAINT PK_GroupTable PRIMARY KEY (groupID),
    CONSTRAINT FK_GroupTable_TO_UserProfile FOREIGN KEY (creatorID)
        REFERENCES UserProfile(userID)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE Friendship(
    requesterID     UUID,                                   -- User that requests the friendship
    targetID        UUID,                                   -- User that receives the friendship
    groupID         UUID,
    specifierID     UUID        NOT NULL,                   -- User who last updated the friendship
    lastUpdate      timestamp   NOT NULL DEFAULT current_timestamp,
    status          VARCHAR,

    CONSTRAINT PK_Friendship PRIMARY KEY (requesterID, targetID),
    CONSTRAINT FK_Frienship_RequesterID_TO_UserProfile FOREIGN KEY (requesterID)
        REFERENCES UserProfile(userID)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
        CONSTRAINT FK_Frienship_TargetID_TO_UserProfile FOREIGN KEY (targetID)
        REFERENCES UserProfile(userID)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    CONSTRAINT FK_Frienship_TO_GroupTable FOREIGN KEY (groupID)
        REFERENCES GroupTable(groupID)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    CONSTRAINT FK_Friendship_SpecifierID_TO_UserProfile FOREIGN KEY (specifierID)
        REFERENCES UserProfile(userID)
        ON DELETE SET NULL
        ON UPDATE CASCADE
    -- Restricción para identificar unívocamente las amistades entre usuarios --
    --CONSTRAINT CHK_FriendshipPKIsUnequivocal CHECK (requesterID < targetID)
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
        REFERENCES UserProfile(userID)
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
        REFERENCES UserProfile(userID)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT FK_Assistance_TO_UserProfile_Invited FOREIGN KEY (invitingUserID)
        REFERENCES UserProfile(userID)
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
        REFERENCES UserProfile(userID)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT FK_Friendship_TO_UserProfile_Invited FOREIGN KEY (invitedUserID)
        REFERENCES UserProfile(userID)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);


/* *************** MESSAGES *************** */
