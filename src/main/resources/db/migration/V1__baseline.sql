-- Baseline: the schema Hibernate generated (ddl-auto) for the thesis-era mapping, as dumped from production on
-- 2026-09-30. Production is baselined at this version (spring.flyway.baseline-on-migrate), so this script only runs on
-- empty databases (tests, new environments). Never edit it; change the schema in a new migration.

CREATE TABLE event_entity (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    description text,
    end_time timestamp(6) without time zone NOT NULL,
    image character varying(255),
    kind character varying(255) NOT NULL,
    price character varying(255),
    start_time timestamp(6) without time zone NOT NULL,
    title character varying(255) NOT NULL,
    owner_id uuid NOT NULL,
    place_id uuid NOT NULL,
    CONSTRAINT event_entity_kind_check CHECK (((kind)::text = ANY ((ARRAY['DISCO'::character varying, 'TECHNO'::character varying, 'FESTIVAL'::character varying, 'JAZZ'::character varying, 'ALTER'::character varying, 'HOME'::character varying, 'PUB'::character varying])::text[])))
);

CREATE TABLE event_lineup_item_entity (
    end_time timestamp(6) without time zone,
    start_time timestamp(6) without time zone,
    performer_id uuid NOT NULL,
    event_id uuid NOT NULL
);

CREATE TABLE event_links (
    event_id uuid NOT NULL,
    type character varying(255) NOT NULL,
    url character varying(255) NOT NULL,
    CONSTRAINT event_links_type_check CHECK (((type)::text = ANY ((ARRAY['INSTAGRAM'::character varying, 'FACEBOOK'::character varying, 'TWITTER'::character varying, 'REDDIT'::character varying, 'WEBSITE'::character varying])::text[])))
);

CREATE TABLE event_plan_entity (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    description character varying(255),
    end_date_time timestamp(6) without time zone NOT NULL,
    image character varying(255),
    kind character varying(255) NOT NULL,
    price character varying(255),
    start_date_time timestamp(6) without time zone NOT NULL,
    title character varying(255),
    owner_sub uuid NOT NULL,
    CONSTRAINT event_plan_entity_kind_check CHECK (((kind)::text = ANY ((ARRAY['DISCO'::character varying, 'TECHNO'::character varying, 'FESTIVAL'::character varying, 'JAZZ'::character varying, 'ALTER'::character varying, 'HOME'::character varying, 'PUB'::character varying])::text[])))
);

CREATE TABLE event_plan_entity_lineup_invitations (
    event_plan_entity_id uuid NOT NULL,
    lineup_invitations_event_plan_id uuid NOT NULL,
    lineup_invitations_performer_id uuid NOT NULL
);

CREATE TABLE event_plan_entity_place_invitations (
    event_plan_entity_id uuid NOT NULL,
    place_invitations_event_plan_id uuid NOT NULL,
    place_invitations_place_id uuid NOT NULL
);

CREATE TABLE event_plan_lineup_invitation_entity (
    end_time timestamp(6) without time zone,
    start_time timestamp(6) without time zone,
    state character varying(255) NOT NULL,
    performer_id uuid NOT NULL,
    event_plan_id uuid NOT NULL,
    CONSTRAINT event_plan_lineup_invitation_entity_state_check CHECK (((state)::text = ANY ((ARRAY['PENDING'::character varying, 'ACCEPTED'::character varying, 'REJECTED'::character varying])::text[])))
);

CREATE TABLE event_plan_links (
    event_plan_id uuid NOT NULL,
    type character varying(255) NOT NULL,
    url character varying(255) NOT NULL,
    CONSTRAINT event_plan_links_type_check CHECK (((type)::text = ANY ((ARRAY['INSTAGRAM'::character varying, 'FACEBOOK'::character varying, 'TWITTER'::character varying, 'REDDIT'::character varying, 'WEBSITE'::character varying])::text[])))
);

CREATE TABLE event_plan_place_invitation_entity (
    state character varying(255) NOT NULL,
    place_id uuid NOT NULL,
    event_plan_id uuid NOT NULL,
    CONSTRAINT event_plan_place_invitation_entity_state_check CHECK (((state)::text = ANY ((ARRAY['PENDING'::character varying, 'ACCEPTED'::character varying, 'REJECTED'::character varying])::text[])))
);

CREATE TABLE performer_entity (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    bio text,
    genre character varying(255) NOT NULL,
    image character varying(255),
    name character varying(255) NOT NULL,
    owner_sub uuid NOT NULL
);

CREATE TABLE performer_entity_lineup_invitations (
    performer_entity_id uuid NOT NULL,
    lineup_invitations_event_plan_id uuid NOT NULL,
    lineup_invitations_performer_id uuid NOT NULL
);

CREATE TABLE performer_entity_lineup_items (
    performer_entity_id uuid NOT NULL,
    lineup_items_event_id uuid NOT NULL,
    lineup_items_performer_id uuid NOT NULL
);

CREATE TABLE performer_links (
    performer_id uuid NOT NULL,
    type character varying(255) NOT NULL,
    url character varying(255) NOT NULL,
    CONSTRAINT performer_links_type_check CHECK (((type)::text = ANY ((ARRAY['INSTAGRAM'::character varying, 'FACEBOOK'::character varying, 'TWITTER'::character varying, 'REDDIT'::character varying, 'WEBSITE'::character varying])::text[])))
);

CREATE TABLE place_entity (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    address character varying(255) NOT NULL,
    city character varying(255) NOT NULL,
    description text,
    image character varying(255),
    latitude double precision,
    longitude double precision,
    name character varying(255) NOT NULL,
    owner_sub uuid NOT NULL
);

CREATE TABLE place_links (
    place_id uuid NOT NULL,
    type character varying(255) NOT NULL,
    url character varying(255) NOT NULL,
    CONSTRAINT place_links_type_check CHECK (((type)::text = ANY ((ARRAY['INSTAGRAM'::character varying, 'FACEBOOK'::character varying, 'TWITTER'::character varying, 'REDDIT'::character varying, 'WEBSITE'::character varying])::text[])))
);

CREATE TABLE place_tags (
    place_id uuid NOT NULL,
    tags character varying(255) NOT NULL
);

CREATE TABLE user_entity (
    sub uuid NOT NULL,
    created_date timestamp(6) with time zone NOT NULL,
    updated_date timestamp(6) with time zone NOT NULL
);

CREATE TABLE user_liked_events (
    user_sub uuid NOT NULL,
    event_id uuid NOT NULL
);

CREATE TABLE user_liked_performers (
    user_sub uuid NOT NULL,
    performer_id uuid NOT NULL
);

CREATE TABLE user_liked_places (
    user_sub uuid NOT NULL,
    place_id uuid NOT NULL
);

ALTER TABLE event_entity
    ADD CONSTRAINT event_entity_pkey PRIMARY KEY (id);

ALTER TABLE event_lineup_item_entity
    ADD CONSTRAINT event_lineup_item_entity_pkey PRIMARY KEY (event_id, performer_id);

ALTER TABLE event_plan_entity
    ADD CONSTRAINT event_plan_entity_pkey PRIMARY KEY (id);

ALTER TABLE event_plan_lineup_invitation_entity
    ADD CONSTRAINT event_plan_lineup_invitation_entity_pkey PRIMARY KEY (event_plan_id, performer_id);

ALTER TABLE event_plan_place_invitation_entity
    ADD CONSTRAINT event_plan_place_invitation_entity_pkey PRIMARY KEY (event_plan_id, place_id);

ALTER TABLE performer_entity
    ADD CONSTRAINT performer_entity_pkey PRIMARY KEY (id);

ALTER TABLE place_entity
    ADD CONSTRAINT place_entity_pkey PRIMARY KEY (id);

ALTER TABLE place_tags
    ADD CONSTRAINT place_tags_pkey PRIMARY KEY (place_id, tags);

ALTER TABLE event_plan_entity_lineup_invitations
    ADD CONSTRAINT uk54wtrjkc0nrle5758qco9su8w UNIQUE (lineup_invitations_event_plan_id, lineup_invitations_performer_id);

ALTER TABLE performer_entity_lineup_items
    ADD CONSTRAINT uk8c670e306ci7mv602s3e4qjtq UNIQUE (lineup_items_event_id, lineup_items_performer_id);

ALTER TABLE performer_entity_lineup_invitations
    ADD CONSTRAINT uknrmxsbtedfygulnub16soq6yl UNIQUE (lineup_invitations_event_plan_id, lineup_invitations_performer_id);

ALTER TABLE event_plan_entity_place_invitations
    ADD CONSTRAINT uktcpe8pv5f5m4ujy0wwiyxgykm UNIQUE (place_invitations_event_plan_id, place_invitations_place_id);

ALTER TABLE user_entity
    ADD CONSTRAINT user_entity_pkey PRIMARY KEY (sub);

ALTER TABLE user_liked_events
    ADD CONSTRAINT user_liked_events_pkey PRIMARY KEY (user_sub, event_id);

ALTER TABLE user_liked_performers
    ADD CONSTRAINT user_liked_performers_pkey PRIMARY KEY (user_sub, performer_id);

ALTER TABLE user_liked_places
    ADD CONSTRAINT user_liked_places_pkey PRIMARY KEY (user_sub, place_id);

ALTER TABLE user_liked_places
    ADD CONSTRAINT fk1cht1etfkve544puvfkw37xe1 FOREIGN KEY (place_id) REFERENCES place_entity(id);

ALTER TABLE event_plan_entity
    ADD CONSTRAINT fk1x37we59nfojxvr9s0k7kb4ac FOREIGN KEY (owner_sub) REFERENCES user_entity(sub);

ALTER TABLE event_plan_entity_lineup_invitations
    ADD CONSTRAINT fk2a3xy7m2c5q60stutoi5ln3p FOREIGN KEY (lineup_invitations_event_plan_id, lineup_invitations_performer_id) REFERENCES event_plan_lineup_invitation_entity(event_plan_id, performer_id);

ALTER TABLE performer_entity_lineup_items
    ADD CONSTRAINT fk300qvx63aa1pgpls8ymsj1448 FOREIGN KEY (performer_entity_id) REFERENCES performer_entity(id);

ALTER TABLE event_plan_entity_lineup_invitations
    ADD CONSTRAINT fk3bl3ysbl4wbwtt9pmkbqkkmy1 FOREIGN KEY (event_plan_entity_id) REFERENCES event_plan_entity(id);

ALTER TABLE event_entity
    ADD CONSTRAINT fk3l2uq9dva3krohay8mi90kur1 FOREIGN KEY (owner_id) REFERENCES user_entity(sub);

ALTER TABLE place_entity
    ADD CONSTRAINT fk3muq99ysjiafyrg274dq4nyo3 FOREIGN KEY (owner_sub) REFERENCES user_entity(sub);

ALTER TABLE user_liked_performers
    ADD CONSTRAINT fk4wo1c0d26yv2vuh3vga2ay2p0 FOREIGN KEY (user_sub) REFERENCES user_entity(sub);

ALTER TABLE user_liked_places
    ADD CONSTRAINT fk4xc1evbg96yixa857alt646wo FOREIGN KEY (user_sub) REFERENCES user_entity(sub);

ALTER TABLE event_plan_entity_place_invitations
    ADD CONSTRAINT fk5lnpv6lj6mrheudiqosec7rfo FOREIGN KEY (event_plan_entity_id) REFERENCES event_plan_entity(id);

ALTER TABLE event_plan_lineup_invitation_entity
    ADD CONSTRAINT fk63tla214xh0tehfmtjyjaj6dx FOREIGN KEY (event_plan_id) REFERENCES event_plan_entity(id);

ALTER TABLE event_plan_lineup_invitation_entity
    ADD CONSTRAINT fk89d18okwd25dwo5vsc5ur5d9g FOREIGN KEY (performer_id) REFERENCES performer_entity(id);

ALTER TABLE event_plan_entity_place_invitations
    ADD CONSTRAINT fk9w1qcr1ch6gu5f31odak1nm4n FOREIGN KEY (place_invitations_event_plan_id, place_invitations_place_id) REFERENCES event_plan_place_invitation_entity(event_plan_id, place_id);

ALTER TABLE event_lineup_item_entity
    ADD CONSTRAINT fkac91t6319amdjw8ckhf091jbx FOREIGN KEY (performer_id) REFERENCES performer_entity(id);

ALTER TABLE user_liked_events
    ADD CONSTRAINT fkadqp2tsp6x360l3kun91e1s0j FOREIGN KEY (event_id) REFERENCES event_entity(id);

ALTER TABLE performer_entity_lineup_invitations
    ADD CONSTRAINT fkbnrd4f5sseu4tc1at5rq6430x FOREIGN KEY (lineup_invitations_event_plan_id, lineup_invitations_performer_id) REFERENCES event_plan_lineup_invitation_entity(event_plan_id, performer_id);

ALTER TABLE event_entity
    ADD CONSTRAINT fkbyjn62u1bg1k35dqivt00nnbo FOREIGN KEY (place_id) REFERENCES place_entity(id);

ALTER TABLE place_links
    ADD CONSTRAINT fkcmgisj6sfdkvi7p8yoc6yymi4 FOREIGN KEY (place_id) REFERENCES place_entity(id);

ALTER TABLE performer_entity_lineup_invitations
    ADD CONSTRAINT fkcqae743vt5kgget2m9pr1l3yf FOREIGN KEY (performer_entity_id) REFERENCES performer_entity(id);

ALTER TABLE user_liked_performers
    ADD CONSTRAINT fkdrtf5a2juvcnglup70w2pqvep FOREIGN KEY (performer_id) REFERENCES performer_entity(id);

ALTER TABLE performer_links
    ADD CONSTRAINT fkfkf4i09wnsxcsyxl0ogvs0a97 FOREIGN KEY (performer_id) REFERENCES performer_entity(id);

ALTER TABLE user_liked_events
    ADD CONSTRAINT fkgqompc3ra38sc244mt7alo307 FOREIGN KEY (user_sub) REFERENCES user_entity(sub);

ALTER TABLE performer_entity_lineup_items
    ADD CONSTRAINT fkgvpxjouyhyuqglhvf94tejdwo FOREIGN KEY (lineup_items_event_id, lineup_items_performer_id) REFERENCES event_lineup_item_entity(event_id, performer_id);

ALTER TABLE place_tags
    ADD CONSTRAINT fkh66bkppg0c0qce9jwer48t1yb FOREIGN KEY (place_id) REFERENCES place_entity(id);

ALTER TABLE event_lineup_item_entity
    ADD CONSTRAINT fkig94074a5atyo54c6bv3nb1wc FOREIGN KEY (event_id) REFERENCES event_entity(id);

ALTER TABLE performer_entity
    ADD CONSTRAINT fkipvkr74okdfbjrrle0n4jwfr6 FOREIGN KEY (owner_sub) REFERENCES user_entity(sub);

ALTER TABLE event_plan_links
    ADD CONSTRAINT fkk41ue1q7p4x3ha02jnsmbmo6 FOREIGN KEY (event_plan_id) REFERENCES event_plan_entity(id);

ALTER TABLE event_plan_place_invitation_entity
    ADD CONSTRAINT fkmuxateakka8t943lnrhj7nyvm FOREIGN KEY (event_plan_id) REFERENCES event_plan_entity(id);

ALTER TABLE event_plan_place_invitation_entity
    ADD CONSTRAINT fkru24cbwr39h9lxugyh63sof6y FOREIGN KEY (place_id) REFERENCES place_entity(id);

ALTER TABLE event_links
    ADD CONSTRAINT fkry2wyhpuutxog9qr2m42by9ed FOREIGN KEY (event_id) REFERENCES event_entity(id);
