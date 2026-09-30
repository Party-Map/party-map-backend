-- Aligns the baseline with the corrected entity mapping (validated by ddl-auto: validate).

-- Join tables Hibernate added for @OneToMany collections that lacked mappedBy. The invitation and lineup tables already
-- hold the same rows (their composite keys point at the plan / event and the performer), so nothing is lost.
DROP TABLE event_plan_entity_place_invitations;
DROP TABLE event_plan_entity_lineup_invitations;
DROP TABLE performer_entity_lineup_invitations;
DROP TABLE performer_entity_lineup_items;

-- Same audit column names as every other table.
ALTER TABLE user_entity RENAME COLUMN created_date TO created_at;
ALTER TABLE user_entity RENAME COLUMN updated_date TO updated_at;

-- Optimistic locking.
ALTER TABLE place_entity ADD COLUMN version bigint NOT NULL DEFAULT 0;
ALTER TABLE performer_entity ADD COLUMN version bigint NOT NULL DEFAULT 0;
ALTER TABLE event_entity ADD COLUMN version bigint NOT NULL DEFAULT 0;
ALTER TABLE event_plan_entity ADD COLUMN version bigint NOT NULL DEFAULT 0;

-- Columns the code always treated as required. Empty text replaces missing text; everything else must already be set.
UPDATE event_entity SET description = '' WHERE description IS NULL;
ALTER TABLE event_entity ALTER COLUMN description SET NOT NULL;
UPDATE performer_entity SET bio = '' WHERE bio IS NULL;
ALTER TABLE performer_entity ALTER COLUMN bio SET NOT NULL;
UPDATE event_plan_entity SET description = '' WHERE description IS NULL;
ALTER TABLE event_plan_entity ALTER COLUMN description TYPE text;
ALTER TABLE event_plan_entity ALTER COLUMN description SET NOT NULL;
ALTER TABLE event_plan_entity ALTER COLUMN title SET NOT NULL;
ALTER TABLE place_entity ALTER COLUMN latitude SET NOT NULL;
ALTER TABLE place_entity ALTER COLUMN longitude SET NOT NULL;

-- A lineup slot without times defaults to the whole event.
UPDATE event_lineup_item_entity li SET start_time = e.start_time FROM event_entity e
WHERE li.event_id = e.id AND li.start_time IS NULL;
UPDATE event_lineup_item_entity li SET end_time = e.end_time FROM event_entity e
WHERE li.event_id = e.id AND li.end_time IS NULL;
ALTER TABLE event_lineup_item_entity ALTER COLUMN start_time SET NOT NULL;
ALTER TABLE event_lineup_item_entity ALTER COLUMN end_time SET NOT NULL;
UPDATE event_plan_lineup_invitation_entity li SET start_time = p.start_date_time FROM event_plan_entity p
WHERE li.event_plan_id = p.id AND li.start_time IS NULL;
UPDATE event_plan_lineup_invitation_entity li SET end_time = p.end_date_time FROM event_plan_entity p
WHERE li.event_plan_id = p.id AND li.end_time IS NULL;
ALTER TABLE event_plan_lineup_invitation_entity ALTER COLUMN start_time SET NOT NULL;
ALTER TABLE event_plan_lineup_invitation_entity ALTER COLUMN end_time SET NOT NULL;

-- Image and link addresses longer than 255 characters (CDN URLs with query strings) failed with a 500.
ALTER TABLE place_entity ALTER COLUMN image TYPE varchar(2048);
ALTER TABLE performer_entity ALTER COLUMN image TYPE varchar(2048);
ALTER TABLE event_entity ALTER COLUMN image TYPE varchar(2048);
ALTER TABLE event_plan_entity ALTER COLUMN image TYPE varchar(2048);
ALTER TABLE place_links ALTER COLUMN url TYPE varchar(2048);
ALTER TABLE performer_links ALTER COLUMN url TYPE varchar(2048);
ALTER TABLE event_links ALTER COLUMN url TYPE varchar(2048);
ALTER TABLE event_plan_links ALTER COLUMN url TYPE varchar(2048);
