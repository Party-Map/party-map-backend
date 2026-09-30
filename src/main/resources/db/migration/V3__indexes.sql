-- Indexes for the lookups the API performs: map bounding box, owner lists, events by place / performer / end time,
-- and invitations by place / performer.
CREATE INDEX place_entity_location_idx ON place_entity (latitude, longitude);
CREATE INDEX place_entity_owner_idx ON place_entity (owner_sub);
CREATE INDEX performer_entity_owner_idx ON performer_entity (owner_sub);
CREATE INDEX event_entity_place_start_idx ON event_entity (place_id, start_time);
CREATE INDEX event_entity_end_time_idx ON event_entity (end_time);
CREATE INDEX event_entity_owner_idx ON event_entity (owner_id);
CREATE INDEX event_lineup_item_entity_performer_idx ON event_lineup_item_entity (performer_id);
CREATE INDEX event_plan_entity_owner_idx ON event_plan_entity (owner_sub);
CREATE INDEX event_plan_place_invitation_entity_place_idx ON event_plan_place_invitation_entity (place_id);
CREATE INDEX event_plan_lineup_invitation_entity_performer_idx ON event_plan_lineup_invitation_entity (performer_id);
