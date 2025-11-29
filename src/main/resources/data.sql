-- ------------------------------------------------------------------
-- Seed user (owner of all places / performers / events) -> user_entity
-- ------------------------------------------------------------------
INSERT INTO public.user_entity (sub, created_date, updated_date)
VALUES ('3241fc43-0124-48ae-8850-eb5ac64559c6', now(), now());

-- ------------------------------------------------------------------
-- Places -> place_entity
-- ------------------------------------------------------------------
INSERT INTO public.place_entity (id, name, latitude, longitude, address, city, description, image, owner_sub,
                                 created_at, updated_at)
VALUES ('6375cdd9-b837-5ac9-a4d6-ae200d4e6059', 'Danube Club', 47.5005, 19.0481, 'Riverbank 12', 'Budapest',
        'Riverside club with two dance floors and a rooftop terrace.',
        'https://images.unsplash.com/photo-1667992403195-d2241a40ca2d?auto=format&fit=crop&w=1280&q=80',
        '3241fc43-0124-48ae-8850-eb5ac64559c6', now(), now()),
       ('d74d2042-627b-55fc-a36e-25cf5bebaf19', 'Ruin Bar 42', 47.4984, 19.0593, 'Kazinczy u. 14', 'Budapest',
        'Iconic ruin-bar vibe with eclectic rooms and a courtyard.',
        'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?q=80&w=1280&auto=format&fit=crop',
        '3241fc43-0124-48ae-8850-eb5ac64559c6', now(), now()),
       ('c12d301a-5c69-5a4c-a1d1-456d3245e247', 'Warehouse X', 47.4869, 19.0701, 'Kőbányai út 21', 'Budapest',
        'Industrial warehouse turned into a late-night techno bunker.',
        'https://images.unsplash.com/photo-1487180144351-b8472da7d491?q=80&w=1280&auto=format&fit=crop',
        '3241fc43-0124-48ae-8850-eb5ac64559c6', now(), now()),
       ('9a169f54-2acb-5b58-b89b-7901297ad1de', 'Silver Shore Club', 46.9062, 18.0491, 'Petőfi sétány 10', 'Siófok',
        'Beachfront stage with sunset sessions and late-night DJs.',
        'https://images.unsplash.com/photo-1526483360412-f4dbaf036963?q=80&w=1280&auto=format&fit=crop',
        '3241fc43-0124-48ae-8850-eb5ac64559c6', now(), now()),
       ('74a4aa03-3493-544b-8c0b-531ba005ed0e', 'Füred Pier Lounge', 46.9606, 17.871, 'Tagore sétány 3', 'Balatonfüred',
        'Lounge by the marina with mellow grooves and cocktails.',
        'https://images.unsplash.com/photo-1506744038136-46273834b3fb?q=80&w=1280&auto=format&fit=crop',
        '3241fc43-0124-48ae-8850-eb5ac64559c6', now(), now()),
       ('64656347-caed-529d-b7b3-bfaea6721865', 'Keszthely Waves', 46.768, 17.243, 'Balaton-part 1', 'Keszthely',
        'Open-air dancefloor a few steps from the water.',
        'https://images.unsplash.com/photo-1514924013411-cbf25faa35bb?q=80&w=1280&auto=format&fit=crop',
        '3241fc43-0124-48ae-8850-eb5ac64559c6', now(), now()),
       ('87709d0c-73c2-5095-b2ae-0763275e76a1', 'Fehérvár Hall', 47.186, 18.4221, 'Palotai út 12', 'Székesfehérvár',
        'Mid-size venue for bass nights and live electronic shows.',
        'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?q=80&w=1280&auto=format&fit=crop',
        '3241fc43-0124-48ae-8850-eb5ac64559c6', now(), now()),
       ('051d4b89-07c4-5f2c-95eb-14b6235484e0', 'Szeged Riverside', 46.253, 20.1414, 'Tisza-part 5', 'Szeged',
        'Neon-lit riverside terrace with synth and retro nights.',
        'https://images.unsplash.com/photo-1520975693415-1a?ixlib=rb-4.0.3&q=80&w=1280&auto=format&fit=crop',
        '3241fc43-0124-48ae-8850-eb5ac64559c6', now(), now()),
       ('43ce6e13-e30b-5b9f-8cc6-0aa52be7cf85', 'Dénes Pince', 47.408248, 19.015077, 'Lépcsős utca 4', 'Budapest',
        'Cozy basement hangout for intimate home gatherings.',
        'https://images.unsplash.com/photo-1519671482749-fd09be7ccebf?q=80&w=1280&auto=format&fit=crop',
        '3241fc43-0124-48ae-8850-eb5ac64559c6', now(), now()),
       ('51377727-7f23-5f98-a5cd-6967c6530234', 'Bence Terasz', 47.424137, 19.014703, 'Zakariás József utca 5',
        'Budapest', 'Private terrace vibe overlooking the quiet suburban streets.',
        'https://images.unsplash.com/photo-1506377247377-2a5b3b417ebb?q=80&w=1280&auto=format&fit=crop',
        '3241fc43-0124-48ae-8850-eb5ac64559c6', now(), now()),
       ('9f1532a9-8b89-5142-a386-147585e1ba33', 'Poldi Bácsi Sörözö', 47.4408014178659, 19.02280223577477,
        'Ady Endre út 95', 'Budapest', 'Cozy local pub for socializing and late-night sessions.',
        'https://images.unsplash.com/photo-1510626176961-4b57d4fbad03?auto=format&fit=crop&w=1280&q=80',
        '3241fc43-0124-48ae-8850-eb5ac64559c6', now(), now());

-- ------------------------------------------------------------------
-- Place tags -> place_tags (tags column)
-- ------------------------------------------------------------------
INSERT INTO public.place_tags (place_id, tags)
VALUES ('6375cdd9-b837-5ac9-a4d6-ae200d4e6059', 'house'),
       ('6375cdd9-b837-5ac9-a4d6-ae200d4e6059', 'techno'),
       ('6375cdd9-b837-5ac9-a4d6-ae200d4e6059', 'rooftop'),
       ('d74d2042-627b-55fc-a36e-25cf5bebaf19', 'ruin bar'),
       ('d74d2042-627b-55fc-a36e-25cf5bebaf19', 'eclectic'),
       ('d74d2042-627b-55fc-a36e-25cf5bebaf19', 'indie'),
       ('c12d301a-5c69-5a4c-a1d1-456d3245e247', 'techno'),
       ('c12d301a-5c69-5a4c-a1d1-456d3245e247', 'underground'),
       ('9a169f54-2acb-5b58-b89b-7901297ad1de', 'beach'),
       ('9a169f54-2acb-5b58-b89b-7901297ad1de', 'house'),
       ('9a169f54-2acb-5b58-b89b-7901297ad1de', 'sunset'),
       ('74a4aa03-3493-544b-8c0b-531ba005ed0e', 'lounge'),
       ('74a4aa03-3493-544b-8c0b-531ba005ed0e', 'deep house'),
       ('74a4aa03-3493-544b-8c0b-531ba005ed0e', 'marina'),
       ('64656347-caed-529d-b7b3-bfaea6721865', 'open-air'),
       ('64656347-caed-529d-b7b3-bfaea6721865', 'tech-house'),
       ('64656347-caed-529d-b7b3-bfaea6721865', 'lake'),
       ('87709d0c-73c2-5095-b2ae-0763275e76a1', 'bass'),
       ('87709d0c-73c2-5095-b2ae-0763275e76a1', 'drum & bass'),
       ('87709d0c-73c2-5095-b2ae-0763275e76a1', 'live'),
       ('051d4b89-07c4-5f2c-95eb-14b6235484e0', 'synthwave'),
       ('051d4b89-07c4-5f2c-95eb-14b6235484e0', 'retro'),
       ('051d4b89-07c4-5f2c-95eb-14b6235484e0', 'terrace'),
       ('43ce6e13-e30b-5b9f-8cc6-0aa52be7cf85', 'chill'),
       ('43ce6e13-e30b-5b9f-8cc6-0aa52be7cf85', 'basement'),
       ('43ce6e13-e30b-5b9f-8cc6-0aa52be7cf85', 'friendly'),
       ('51377727-7f23-5f98-a5cd-6967c6530234', 'chill'),
       ('51377727-7f23-5f98-a5cd-6967c6530234', 'terrace'),
       ('51377727-7f23-5f98-a5cd-6967c6530234', 'friendly'),
       ('9f1532a9-8b89-5142-a386-147585e1ba33', 'alter'),
       ('9f1532a9-8b89-5142-a386-147585e1ba33', 'techno'),
       ('9f1532a9-8b89-5142-a386-147585e1ba33', 'socializing'),
       ('9f1532a9-8b89-5142-a386-147585e1ba33', 'pub');

-- ------------------------------------------------------------------
-- Performers -> performer_entity
-- ------------------------------------------------------------------
INSERT INTO public.performer_entity (id, name, genre, bio, image, owner_sub, created_at, updated_at)
VALUES ('04238ef3-0e2b-528d-b141-ab202c578afc', 'DJ Aurora', 'Melodic Techno',
        'Budapest-based DJ known for atmospheric sets and sunrise closers.',
        'https://images.unsplash.com/photo-1516280440614-37939bbacd81?q=80&w=1280&auto=format&fit=crop',
        '3241fc43-0124-48ae-8850-eb5ac64559c6', now(), now()),
       ('0d19069c-1a50-5cbc-8e32-ea7e162f9b4c', 'MC Lumen', 'Hip-Hop', 'High-energy MC bringing the party to life.',
        'https://images.unsplash.com/photo-1506157786151-b8491531f063?q=80&w=1280&auto=format&fit=crop',
        '3241fc43-0124-48ae-8850-eb5ac64559c6', now(), now()),
       ('0667375d-88a4-58ca-8ad1-785f2bdcf1ce', 'Klang Duo', 'House', 'Back-to-back house duo with classic grooves.',
        'https://images.unsplash.com/photo-1548429859-7e7456e610b7?q=80&w=1280&auto=format&fit=crop',
        '3241fc43-0124-48ae-8850-eb5ac64559c6', now(), now()),
       ('54b88841-f7cc-53fc-8595-a89b8b11aa1c', 'DJ Balcsi', 'Beach House',
        'Feel-good beach house inspired by Balaton sunsets.',
        'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?q=80&w=1280&auto=format&fit=crop',
        '3241fc43-0124-48ae-8850-eb5ac64559c6', now(), now()),
       ('363ed242-4068-5d2b-8750-ea2ece66d59d', 'Vibe Knights', 'Drum & Bass',
        'Two-person DnB unit known for tight rollers.',
        'https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?q=80&w=1280&auto=format&fit=crop',
        '3241fc43-0124-48ae-8850-eb5ac64559c6', now(), now()),
       ('c1d21f9e-f293-5275-96db-242842b09ed3', 'Szeged Synth', 'Synthwave',
        'Retro-futurist live act with neon-soaked arps.',
        'https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=1280&auto=format&fit=crop',
        '3241fc43-0124-48ae-8850-eb5ac64559c6', now(), now());

-- Performer links (same structure) -> performer_links
INSERT INTO public.performer_links (performer_id, type, url)
VALUES ('04238ef3-0e2b-528d-b141-ab202c578afc', 'INSTAGRAM', 'https://instagram.com/djaurora'),
       ('04238ef3-0e2b-528d-b141-ab202c578afc', 'WEBSITE', 'https://aurora.example.com');

-- ------------------------------------------------------------------
-- Events -> event_entity
-- ------------------------------------------------------------------
INSERT INTO public.event_entity (id, title, place_id, description, start_time, end_time, image, price, kind, owner_id,
                                 created_at, updated_at)
VALUES ('23c3c9fb-23e6-59d7-b317-dd614478e685', 'Sunset Sessions', '6375cdd9-b837-5ac9-a4d6-ae200d4e6059',
        'Open-air evening by the river with melodic vibes.', now() + interval '1 day', now() + interval '2 days',
        'https://images.unsplash.com/photo-1540040582279-4d6cdf2d1b8b?q=80&w=1280&auto=format&fit=crop', '€15', 'DISCO',
        '3241fc43-0124-48ae-8850-eb5ac64559c6', now(), now()),
       ('adc3b4aa-ffe7-5cd2-b2e9-6bc06eceb2e2', 'Basement Breaks', 'd74d2042-627b-55fc-a36e-25cf5bebaf19',
        'Indie and alt mixes in the classic ruin bar setting.', now() + interval '3 days',
        now() + interval '3 days 4 hours',
        'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?q=80&w=1280&auto=format&fit=crop', 'Free',
        'ALTER', '3241fc43-0124-48ae-8850-eb5ac64559c6', now(), now()),
       ('cd931a12-7697-588b-a6c4-603d2589f7aa', 'Warehouse All-Nighter', 'c12d301a-5c69-5a4c-a1d1-456d3245e247',
        'Raw, pounding techno until sunrise.', now() + interval '5 days', now() + interval '6 days',
        'https://images.unsplash.com/photo-1487180144351-b8472da7d491?q=80&w=1280&auto=format&fit=crop', '€20',
        'TECHNO', '3241fc43-0124-48ae-8850-eb5ac64559c6', now(), now()),
       ('ee3a5589-54fc-50f2-a9b0-60056cf59478', 'Shoreline Sunset', '9a169f54-2acb-5b58-b89b-7901297ad1de',
        'Beach house and chilled grooves as the sun dips over Balaton.', now() + interval '2 days 18 hours',
        now() + interval '3 days 1 hour',
        'https://images.unsplash.com/photo-1515706886582-54c73c5eaf41?q=80&w=1280&auto=format&fit=crop', 'HUF 4,500',
        'FESTIVAL', '3241fc43-0124-48ae-8850-eb5ac64559c6', now(), now()),
       ('7862fe14-5a13-5df2-96e9-1d0f55eb671a', 'Pier Nights', '74a4aa03-3493-544b-8c0b-531ba005ed0e',
        'Deep house on the pier with mellow lights and the marina breeze.', now() + interval '4 days 20 hours',
        now() + interval '5 days 2 hours',
        'https://images.unsplash.com/photo-1506157786151-b8491531f063?q=80&w=1280&auto=format&fit=crop', 'HUF 3,900',
        'DISCO', '3241fc43-0124-48ae-8850-eb5ac64559c6', now(), now()),
       ('f04864d6-cad9-5161-89d4-ede7a25d1efa', 'Waves Afterdark', '64656347-caed-529d-b7b3-bfaea6721865',
        'Tech-house rhythms with the lake at your feet.', now() + interval '6 days 22 hours',
        now() + interval '7 days 5 hours',
        'https://images.unsplash.com/photo-1492684223066-81342ee5ff30?q=80&w=1280&auto=format&fit=crop', 'HUF 5,200',
        'TECHNO', '3241fc43-0124-48ae-8850-eb5ac64559c6', now(), now()),
       ('fa4860b4-daf2-5dc8-b7b6-dd769ea9c363', 'Fehérvár Bassline', '87709d0c-73c2-5095-b2ae-0763275e76a1',
        'Local DnB heads unite for a night of rollers and halftime.', now() + interval '3 days 21 hours',
        now() + interval '4 days 3 hours',
        'https://images.unsplash.com/photo-1508214751196-bcfd4ca60f91?q=80&w=1280&auto=format&fit=crop', 'HUF 3,500',
        'ALTER', '3241fc43-0124-48ae-8850-eb5ac64559c6', now(), now()),
       ('cd83795e-7909-5a5a-b60b-cd94cf20ae8c', 'Tisza Neon Ride', '051d4b89-07c4-5f2c-95eb-14b6235484e0',
        'Neon synthwave night on the riverfront.', now() + interval '8 days 20 hours',
        now() + interval '9 days 2 hours',
        'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?q=80&w=1280&auto=format&fit=crop', 'HUF 4,200',
        'JAZZ', '3241fc43-0124-48ae-8850-eb5ac64559c6', now(), now()),
       ('50bf3153-7d1c-51d2-9bb7-cc81432d7311', 'Buli a pincébe', '43ce6e13-e30b-5b9f-8cc6-0aa52be7cf85',
        'Laid-back basement home party with friendly crowd.', now() - interval '1 hour',
        now() + interval '3 days 2 hours',
        'https://images.unsplash.com/photo-1646184466560-f81b1e495604?auto=format&fit=crop&w=1280&q=80', 'Free', 'HOME',
        '3241fc43-0124-48ae-8850-eb5ac64559c6', now(), now()),
       ('45333217-d63f-5b51-a603-6755d84f914e', 'Masik Buli a pincébe', '43ce6e13-e30b-5b9f-8cc6-0aa52be7cf85',
        'Laid-back basement home party with friendly crowd.', now() + interval '3 days 2 hours',
        now() + interval '3 days 5 hours',
        'https://images.unsplash.com/photo-1646184466560-f81b1e495604?auto=format&fit=crop&w=1280&q=80', 'Free', 'HOME',
        '3241fc43-0124-48ae-8850-eb5ac64559c6', now(), now()),
       ('b60740a3-aa4b-5f9b-af47-a63ba2f5c3d6', 'Terasz Buli', '51377727-7f23-5f98-a5cd-6967c6530234',
        'Chill terrace evening with good friends and mellow tunes.', now() + interval '4 days 19 hours',
        now() + interval '5 days 1 hour',
        'https://images.unsplash.com/photo-1667992403195-d2241a40ca2d?auto=format&fit=crop&w=1280&q=80', 'Free', 'HOME',
        '3241fc43-0124-48ae-8850-eb5ac64559c6', now(), now()),
       ('271bfe96-3dd8-579d-9279-d0f5b1194f4c', 'Mértékkel ivás', '9f1532a9-8b89-5142-a386-147585e1ba33',
        'Deep session of social drinking and beats.', now() + interval '2 days 20 hours',
        now() + interval '3 days 2 hours',
        'https://images.unsplash.com/photo-1509042239860-f550ce710b93?auto=format&fit=crop&w=1280&q=80', 'Free', 'PUB',
        '3241fc43-0124-48ae-8850-eb5ac64559c6', now(), now());

-- ------------------------------------------------------------------
-- Event ↔ Performer relations
-- Old: event_performers(event_id, performer_id)
-- New:
--   event_lineup_item_entity(event_id, performer_id, start_time, end_time)
--   performer_entity_lineup_items(lineup_items_event_id, lineup_items_performer_id, performer_entity_id)
-- We seed with NULL times.
-- ------------------------------------------------------------------

-- Basic lineup items WITH times
INSERT INTO public.event_lineup_item_entity (event_id, performer_id, start_time, end_time)
VALUES
    -- Sunset Sessions: DJ Aurora (single)
    ('23c3c9fb-23e6-59d7-b317-dd614478e685', '04238ef3-0e2b-528d-b141-ab202c578afc',
     TIME '22:00', TIME '01:00'),

    -- Basement Breaks: Klang Duo (single)
    ('adc3b4aa-ffe7-5cd2-b2e9-6bc06eceb2e2', '0667375d-88a4-58ca-8ad1-785f2bdcf1ce',
     TIME '22:00', TIME '01:00'),

    -- Warehouse All-Nighter: DJ Aurora + MC Lumen (two performers)
    ('cd931a12-7697-588b-a6c4-603d2589f7aa', '04238ef3-0e2b-528d-b141-ab202c578afc',
     TIME '22:00', TIME '00:00'),
    ('cd931a12-7697-588b-a6c4-603d2589f7aa', '0d19069c-1a50-5cbc-8e32-ea7e162f9b4c',
     TIME '00:00', TIME '02:00'),

    -- Shoreline Sunset: DJ Balcsi (single)
    ('ee3a5589-54fc-50f2-a9b0-60056cf59478', '54b88841-f7cc-53fc-8595-a89b8b11aa1c',
     TIME '18:00', TIME '21:00'),

    -- Pier Nights: DJ Aurora + DJ Balcsi (two performers)
    ('7862fe14-5a13-5df2-96e9-1d0f55eb671a', '04238ef3-0e2b-528d-b141-ab202c578afc',
     TIME '21:00', TIME '23:00'),
    ('7862fe14-5a13-5df2-96e9-1d0f55eb671a', '54b88841-f7cc-53fc-8595-a89b8b11aa1c',
     TIME '23:00', TIME '01:00'),

    -- Waves Afterdark: Klang Duo (single)
    ('f04864d6-cad9-5161-89d4-ede7a25d1efa', '0667375d-88a4-58ca-8ad1-785f2bdcf1ce',
     TIME '22:00', TIME '02:00'),

    -- Fehérvár Bassline: Vibe Knights (single)
    ('fa4860b4-daf2-5dc8-b7b6-dd769ea9c363', '363ed242-4068-5d2b-8750-ea2ece66d59d',
     TIME '22:00', TIME '01:00'),

    -- Tisza Neon Ride: Szeged Synth (single)
    ('cd83795e-7909-5a5a-b60b-cd94cf20ae8c', 'c1d21f9e-f293-5275-96db-242842b09ed3',
     TIME '21:00', TIME '00:00');

-- Mirror for performer_entity_lineup_items (unchanged)
INSERT INTO public.performer_entity_lineup_items (lineup_items_event_id, lineup_items_performer_id, performer_entity_id)
VALUES ('23c3c9fb-23e6-59d7-b317-dd614478e685', '04238ef3-0e2b-528d-b141-ab202c578afc',
        '04238ef3-0e2b-528d-b141-ab202c578afc'),
       ('adc3b4aa-ffe7-5cd2-b2e9-6bc06eceb2e2', '0667375d-88a4-58ca-8ad1-785f2bdcf1ce',
        '0667375d-88a4-58ca-8ad1-785f2bdcf1ce'),
       ('cd931a12-7697-588b-a6c4-603d2589f7aa', '04238ef3-0e2b-528d-b141-ab202c578afc',
        '04238ef3-0e2b-528d-b141-ab202c578afc'),
       ('cd931a12-7697-588b-a6c4-603d2589f7aa', '0d19069c-1a50-5cbc-8e32-ea7e162f9b4c',
        '0d19069c-1a50-5cbc-8e32-ea7e162f9b4c'),
       ('ee3a5589-54fc-50f2-a9b0-60056cf59478', '54b88841-f7cc-53fc-8595-a89b8b11aa1c',
        '54b88841-f7cc-53fc-8595-a89b8b11aa1c'),
       ('7862fe14-5a13-5df2-96e9-1d0f55eb671a', '04238ef3-0e2b-528d-b141-ab202c578afc',
        '04238ef3-0e2b-528d-b141-ab202c578afc'),
       ('7862fe14-5a13-5df2-96e9-1d0f55eb671a', '54b88841-f7cc-53fc-8595-a89b8b11aa1c',
        '54b88841-f7cc-53fc-8595-a89b8b11aa1c'),
       ('f04864d6-cad9-5161-89d4-ede7a25d1efa', '0667375d-88a4-58ca-8ad1-785f2bdcf1ce',
        '0667375d-88a4-58ca-8ad1-785f2bdcf1ce'),
       ('fa4860b4-daf2-5dc8-b7b6-dd769ea9c363', '363ed242-4068-5d2b-8750-ea2ece66d59d',
        '363ed242-4068-5d2b-8750-ea2ece66d59d'),
       ('cd83795e-7909-5a5a-b60b-cd94cf20ae8c', 'c1d21f9e-f293-5275-96db-242842b09ed3',
        'c1d21f9e-f293-5275-96db-242842b09ed3');


INSERT INTO public.event_plan_entity (created_at, end_date_time, start_date_time, updated_at, id, owner_sub,
                                      description, image, kind, price, title)
VALUES ('2025-11-29 15:28:30.599879 +00:00', '2025-12-05 14:00:00.000000', '2025-12-04 11:00:00.000000',
        '2025-11-29 15:28:30.599879 +00:00', 'a471c74e-c517-45ea-8173-c0f6b79ea854',
        '3241fc43-0124-48ae-8850-eb5ac64559c6', 'Description of test event plan',
        'https://images.unsplash.com/photo-1512830414785-9928e23475dc?q=80&w=1170&auto=format&fit=crop', 'PUB',
        '54546874684', 'Test asd');
