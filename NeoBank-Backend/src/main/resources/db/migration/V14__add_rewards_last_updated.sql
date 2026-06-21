-- V14: Add last_updated column to rewards table if it does not exist
-- This stored procedure conditionally adds the column to avoid errors on databases where it was already created manually.

DROP PROCEDURE IF EXISTS AddLastUpdatedToRewards;

DELIMITER //

CREATE PROCEDURE AddLastUpdatedToRewards()
BEGIN
    DECLARE col_exists INT;
    
    SELECT COUNT(*) INTO col_exists
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'rewards'
      AND COLUMN_NAME = 'last_updated';
      
    IF col_exists = 0 THEN
        ALTER TABLE rewards ADD COLUMN last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
    END IF;
END //

DELIMITER ;

CALL AddLastUpdatedToRewards();

DROP PROCEDURE AddLastUpdatedToRewards;
