-- If a DB update was performed, insert a new line with a comment to the table SCHEMA_VERSION.
-- Example: INSERT INTO schema_version (version_number, comment) VALUES (1, 'Init database');

-- #1
CREATE TABLE schema_version
(
  version_number integer NOT NULL,
  changedate timestamp without time zone NOT NULL DEFAULT now(),
  comment character varying(255),
  CONSTRAINT schema_version_pkey PRIMARY KEY (version_number )
)
WITH (
  OIDS=FALSE
);

ALTER TABLE schema_version OWNER TO sormas_user;

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- TOC entry 483 (class 2612 OID 11574)
-- Name: plpgsql; Type: PROCEDURAL LANGUAGE; Schema: -; Owner: postgres
--

SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 142 (class 1259 OID 341910)
-- Name: cases; Type: TABLE; Schema: public; Owner: sormas_user; Tablespace:
--

CREATE TABLE cases (
    id bigint NOT NULL,
    casestatus character varying(255),
    changedate timestamp without time zone NOT NULL,
    confirmeddate timestamp without time zone,
    creationdate timestamp without time zone NOT NULL,
    description character varying(512),
    disease character varying(255),
    investigateddate timestamp without time zone,
    negativedate timestamp without time zone,
    nocasedate timestamp without time zone,
    postivedate timestamp without time zone,
    recovereddate timestamp without time zone,
    reportdate timestamp without time zone,
    suspectdate timestamp without time zone,
    uuid character varying(36) NOT NULL,
    caseofficer_id bigint,
    casesupervisor_id bigint,
    contactofficer_id bigint,
    contactsupervisor_id bigint,
    healthfacility_id bigint,
    illlocation_id bigint,
    reportinguser_id bigint,
    surveillanceofficer_id bigint,
    surveillancesupervisor_id bigint,
    person_id bigint NOT NULL
);


ALTER TABLE cases OWNER TO sormas_user;

--
-- TOC entry 148 (class 1259 OID 341964)
-- Name: community; Type: TABLE; Schema: public; Owner: sormas_user; Tablespace:
--

CREATE TABLE community (
    id bigint NOT NULL,
    changedate timestamp without time zone NOT NULL,
    creationdate timestamp without time zone NOT NULL,
    name character varying(255),
    uuid character varying(36) NOT NULL,
    district_id bigint NOT NULL
);


ALTER TABLE community OWNER TO sormas_user;

--
-- TOC entry 147 (class 1259 OID 341957)
-- Name: district; Type: TABLE; Schema: public; Owner: sormas_user; Tablespace:
--

CREATE TABLE district (
    id bigint NOT NULL,
    changedate timestamp without time zone NOT NULL,
    creationdate timestamp without time zone NOT NULL,
    name character varying(255),
    uuid character varying(36) NOT NULL,
    region_id bigint NOT NULL
);


ALTER TABLE district OWNER TO sormas_user;

--
-- TOC entry 151 (class 1259 OID 342106)
-- Name: entity_seq; Type: SEQUENCE; Schema: public; Owner: sormas_user
--

CREATE SEQUENCE entity_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE entity_seq OWNER TO sormas_user;

--
-- TOC entry 149 (class 1259 OID 341971)
-- Name: facility; Type: TABLE; Schema: public; Owner: sormas_user; Tablespace:
--

CREATE TABLE facility (
    id bigint NOT NULL,
    changedate timestamp without time zone NOT NULL,
    creationdate timestamp without time zone NOT NULL,
    name character varying(255),
    publicownership boolean,
    type character varying(255),
    uuid character varying(36) NOT NULL,
    location_id bigint
);


ALTER TABLE facility OWNER TO sormas_user;

--
-- TOC entry 144 (class 1259 OID 341930)
-- Name: location; Type: TABLE; Schema: public; Owner: sormas_user; Tablespace:
--

CREATE TABLE location (
    id bigint NOT NULL,
    address character varying(255),
    changedate timestamp without time zone NOT NULL,
    city character varying(255),
    creationdate timestamp without time zone NOT NULL,
    details character varying(255),
    latitude double precision,
    longitude double precision,
    uuid character varying(36) NOT NULL,
    community_id bigint,
    district_id bigint,
    region_id bigint
);


ALTER TABLE location OWNER TO sormas_user;

--
-- TOC entry 143 (class 1259 OID 341920)
-- Name: person; Type: TABLE; Schema: public; Owner: sormas_user; Tablespace:
--

CREATE TABLE person (
    id bigint NOT NULL,
    approximateage integer,
    approximateagetype integer,
    birthdate date,
    burialconductor character varying(255),
    burialdate date,
    changedate timestamp without time zone NOT NULL,
    creationdate timestamp without time zone NOT NULL,
    dead boolean,
    deathdate date,
    firstname character varying(255) NOT NULL,
    lastname character varying(255) NOT NULL,
    occupationdetails character varying(255),
    occupationtype character varying(255),
    phone character varying(255),
    presentcondition integer,
    sex character varying(255),
    uuid character varying(36) NOT NULL,
    address_id bigint,
    buriallocation_id bigint,
    deathlocation_id bigint,
    occupationfacility_id bigint
);


ALTER TABLE person OWNER TO sormas_user;

--
-- TOC entry 146 (class 1259 OID 341950)
-- Name: region; Type: TABLE; Schema: public; Owner: sormas_user; Tablespace:
--

CREATE TABLE region (
    id bigint NOT NULL,
    changedate timestamp without time zone NOT NULL,
    creationdate timestamp without time zone NOT NULL,
    name character varying(255),
    uuid character varying(36) NOT NULL
);


ALTER TABLE region OWNER TO sormas_user;

--
-- TOC entry 150 (class 1259 OID 341981)
-- Name: userroles; Type: TABLE; Schema: public; Owner: sormas_user; Tablespace:
--

CREATE TABLE userroles (
    user_id bigint NOT NULL,
    userrole character varying(255) NOT NULL
);


ALTER TABLE userroles OWNER TO sormas_user;

--
-- TOC entry 145 (class 1259 OID 341940)
-- Name: users; Type: TABLE; Schema: public; Owner: sormas_user; Tablespace:
--

CREATE TABLE users (
    id bigint NOT NULL,
    aktiv boolean NOT NULL,
    changedate timestamp without time zone NOT NULL,
    creationdate timestamp without time zone NOT NULL,
    firstname character varying(255) NOT NULL,
    lastname character varying(255) NOT NULL,
    password character varying(64) NOT NULL,
    phone character varying(255),
    seed character varying(16) NOT NULL,
    useremail character varying(255),
    username character varying(255) NOT NULL,
    uuid character varying(36) NOT NULL,
    address_id bigint,
    associatedofficer_id bigint,
    region_id bigint
);


ALTER TABLE users OWNER TO sormas_user;

--
-- TOC entry 1732 (class 2606 OID 341917)
-- Name: cases_pkey; Type: CONSTRAINT; Schema: public; Owner: sormas_user; Tablespace:
--

ALTER TABLE ONLY cases
    ADD CONSTRAINT cases_pkey PRIMARY KEY (id);


--
-- TOC entry 1734 (class 2606 OID 341919)
-- Name: cases_uuid_key; Type: CONSTRAINT; Schema: public; Owner: sormas_user; Tablespace:
--

ALTER TABLE ONLY cases
    ADD CONSTRAINT cases_uuid_key UNIQUE (uuid);


--
-- TOC entry 1756 (class 2606 OID 341968)
-- Name: community_pkey; Type: CONSTRAINT; Schema: public; Owner: sormas_user; Tablespace:
--

ALTER TABLE ONLY community
    ADD CONSTRAINT community_pkey PRIMARY KEY (id);


--
-- TOC entry 1758 (class 2606 OID 341970)
-- Name: community_uuid_key; Type: CONSTRAINT; Schema: public; Owner: sormas_user; Tablespace:
--

ALTER TABLE ONLY community
    ADD CONSTRAINT community_uuid_key UNIQUE (uuid);


--
-- TOC entry 1752 (class 2606 OID 341961)
-- Name: district_pkey; Type: CONSTRAINT; Schema: public; Owner: sormas_user; Tablespace:
--

ALTER TABLE ONLY district
    ADD CONSTRAINT district_pkey PRIMARY KEY (id);


--
-- TOC entry 1754 (class 2606 OID 341963)
-- Name: district_uuid_key; Type: CONSTRAINT; Schema: public; Owner: sormas_user; Tablespace:
--

ALTER TABLE ONLY district
    ADD CONSTRAINT district_uuid_key UNIQUE (uuid);


--
-- TOC entry 1760 (class 2606 OID 341978)
-- Name: facility_pkey; Type: CONSTRAINT; Schema: public; Owner: sormas_user; Tablespace:
--

ALTER TABLE ONLY facility
    ADD CONSTRAINT facility_pkey PRIMARY KEY (id);


--
-- TOC entry 1762 (class 2606 OID 341980)
-- Name: facility_uuid_key; Type: CONSTRAINT; Schema: public; Owner: sormas_user; Tablespace:
--

ALTER TABLE ONLY facility
    ADD CONSTRAINT facility_uuid_key UNIQUE (uuid);


--
-- TOC entry 1740 (class 2606 OID 341937)
-- Name: location_pkey; Type: CONSTRAINT; Schema: public; Owner: sormas_user; Tablespace:
--

ALTER TABLE ONLY location
    ADD CONSTRAINT location_pkey PRIMARY KEY (id);


--
-- TOC entry 1742 (class 2606 OID 341939)
-- Name: location_uuid_key; Type: CONSTRAINT; Schema: public; Owner: sormas_user; Tablespace:
--

ALTER TABLE ONLY location
    ADD CONSTRAINT location_uuid_key UNIQUE (uuid);


--
-- TOC entry 1736 (class 2606 OID 341927)
-- Name: person_pkey; Type: CONSTRAINT; Schema: public; Owner: sormas_user; Tablespace:
--

ALTER TABLE ONLY person
    ADD CONSTRAINT person_pkey PRIMARY KEY (id);


--
-- TOC entry 1738 (class 2606 OID 341929)
-- Name: person_uuid_key; Type: CONSTRAINT; Schema: public; Owner: sormas_user; Tablespace:
--

ALTER TABLE ONLY person
    ADD CONSTRAINT person_uuid_key UNIQUE (uuid);


--
-- TOC entry 1748 (class 2606 OID 341954)
-- Name: region_pkey; Type: CONSTRAINT; Schema: public; Owner: sormas_user; Tablespace:
--

ALTER TABLE ONLY region
    ADD CONSTRAINT region_pkey PRIMARY KEY (id);


--
-- TOC entry 1750 (class 2606 OID 341956)
-- Name: region_uuid_key; Type: CONSTRAINT; Schema: public; Owner: sormas_user; Tablespace:
--

ALTER TABLE ONLY region
    ADD CONSTRAINT region_uuid_key UNIQUE (uuid);


--
-- TOC entry 1764 (class 2606 OID 341985)
-- Name: unq_userroles_0; Type: CONSTRAINT; Schema: public; Owner: sormas_user; Tablespace:
--

ALTER TABLE ONLY userroles
    ADD CONSTRAINT unq_userroles_0 UNIQUE (user_id, userrole);


--
-- TOC entry 1744 (class 2606 OID 341947)
-- Name: users_pkey; Type: CONSTRAINT; Schema: public; Owner: sormas_user; Tablespace:
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- TOC entry 1746 (class 2606 OID 341949)
-- Name: users_uuid_key; Type: CONSTRAINT; Schema: public; Owner: sormas_user; Tablespace:
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_uuid_key UNIQUE (uuid);


--
-- TOC entry 1765 (class 2606 OID 341986)
-- Name: fk_cases_caseofficer_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY cases
    ADD CONSTRAINT fk_cases_caseofficer_id FOREIGN KEY (caseofficer_id) REFERENCES users(id);


--
-- TOC entry 1769 (class 2606 OID 342006)
-- Name: fk_cases_casesupervisor_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY cases
    ADD CONSTRAINT fk_cases_casesupervisor_id FOREIGN KEY (casesupervisor_id) REFERENCES users(id);


--
-- TOC entry 1770 (class 2606 OID 342011)
-- Name: fk_cases_contactofficer_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY cases
    ADD CONSTRAINT fk_cases_contactofficer_id FOREIGN KEY (contactofficer_id) REFERENCES users(id);


--
-- TOC entry 1768 (class 2606 OID 342001)
-- Name: fk_cases_contactsupervisor_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY cases
    ADD CONSTRAINT fk_cases_contactsupervisor_id FOREIGN KEY (contactsupervisor_id) REFERENCES users(id);


--
-- TOC entry 1772 (class 2606 OID 342021)
-- Name: fk_cases_healthfacility_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY cases
    ADD CONSTRAINT fk_cases_healthfacility_id FOREIGN KEY (healthfacility_id) REFERENCES facility(id);


--
-- TOC entry 1773 (class 2606 OID 342026)
-- Name: fk_cases_illlocation_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY cases
    ADD CONSTRAINT fk_cases_illlocation_id FOREIGN KEY (illlocation_id) REFERENCES location(id);


--
-- TOC entry 1767 (class 2606 OID 341996)
-- Name: fk_cases_person_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY cases
    ADD CONSTRAINT fk_cases_person_id FOREIGN KEY (person_id) REFERENCES person(id);


--
-- TOC entry 1766 (class 2606 OID 341991)
-- Name: fk_cases_reportinguser_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY cases
    ADD CONSTRAINT fk_cases_reportinguser_id FOREIGN KEY (reportinguser_id) REFERENCES users(id);


--
-- TOC entry 1774 (class 2606 OID 342031)
-- Name: fk_cases_surveillanceofficer_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY cases
    ADD CONSTRAINT fk_cases_surveillanceofficer_id FOREIGN KEY (surveillanceofficer_id) REFERENCES users(id);


--
-- TOC entry 1771 (class 2606 OID 342016)
-- Name: fk_cases_surveillancesupervisor_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY cases
    ADD CONSTRAINT fk_cases_surveillancesupervisor_id FOREIGN KEY (surveillancesupervisor_id) REFERENCES users(id);


--
-- TOC entry 1786 (class 2606 OID 342091)
-- Name: fk_community_district_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY community
    ADD CONSTRAINT fk_community_district_id FOREIGN KEY (district_id) REFERENCES district(id);


--
-- TOC entry 1785 (class 2606 OID 342086)
-- Name: fk_district_region_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY district
    ADD CONSTRAINT fk_district_region_id FOREIGN KEY (region_id) REFERENCES region(id);


--
-- TOC entry 1787 (class 2606 OID 342096)
-- Name: fk_facility_location_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY facility
    ADD CONSTRAINT fk_facility_location_id FOREIGN KEY (location_id) REFERENCES location(id);


--
-- TOC entry 1779 (class 2606 OID 342056)
-- Name: fk_location_community_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY location
    ADD CONSTRAINT fk_location_community_id FOREIGN KEY (community_id) REFERENCES community(id);


--
-- TOC entry 1781 (class 2606 OID 342066)
-- Name: fk_location_district_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY location
    ADD CONSTRAINT fk_location_district_id FOREIGN KEY (district_id) REFERENCES district(id);


--
-- TOC entry 1780 (class 2606 OID 342061)
-- Name: fk_location_region_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY location
    ADD CONSTRAINT fk_location_region_id FOREIGN KEY (region_id) REFERENCES region(id);


--
-- TOC entry 1778 (class 2606 OID 342051)
-- Name: fk_person_address_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY person
    ADD CONSTRAINT fk_person_address_id FOREIGN KEY (address_id) REFERENCES location(id);


--
-- TOC entry 1776 (class 2606 OID 342041)
-- Name: fk_person_buriallocation_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY person
    ADD CONSTRAINT fk_person_buriallocation_id FOREIGN KEY (buriallocation_id) REFERENCES location(id);


--
-- TOC entry 1775 (class 2606 OID 342036)
-- Name: fk_person_deathlocation_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY person
    ADD CONSTRAINT fk_person_deathlocation_id FOREIGN KEY (deathlocation_id) REFERENCES location(id);


--
-- TOC entry 1777 (class 2606 OID 342046)
-- Name: fk_person_occupationfacility_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY person
    ADD CONSTRAINT fk_person_occupationfacility_id FOREIGN KEY (occupationfacility_id) REFERENCES facility(id);


--
-- TOC entry 1788 (class 2606 OID 342101)
-- Name: fk_userroles_user_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY userroles
    ADD CONSTRAINT fk_userroles_user_id FOREIGN KEY (user_id) REFERENCES users(id);


--
-- TOC entry 1783 (class 2606 OID 342076)
-- Name: fk_users_address_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY users
    ADD CONSTRAINT fk_users_address_id FOREIGN KEY (address_id) REFERENCES location(id);


--
-- TOC entry 1784 (class 2606 OID 342081)
-- Name: fk_users_associatedofficer_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY users
    ADD CONSTRAINT fk_users_associatedofficer_id FOREIGN KEY (associatedofficer_id) REFERENCES users(id);


--
-- TOC entry 1782 (class 2606 OID 342071)
-- Name: fk_users_region_id; Type: FK CONSTRAINT; Schema: public; Owner: sormas_user
--

ALTER TABLE ONLY users
    ADD CONSTRAINT fk_users_region_id FOREIGN KEY (region_id) REFERENCES region(id);


--
-- TOC entry 1881 (class 0 OID 0)
-- Dependencies: 5
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

ALTER TABLE person ADD COLUMN phoneowner character varying(255);

INSERT INTO schema_version (version_number, comment) VALUES (1, 'Init database');


-- 2016-09-27; #55; sszczesny
ALTER TABLE person DROP COLUMN birthdate;
ALTER TABLE person ADD COLUMN birthdate_dd integer;
ALTER TABLE person ADD COLUMN birthdate_mm integer;
ALTER TABLE person ADD COLUMN birthdate_yyyy integer;

INSERT INTO schema_version (version_number, comment) VALUES (2, 'Split person birthdate year, month, day');


-- 2016-10-04; #58
CREATE TABLE symptoms (
id bigint not null,
abdominalpain varchar(255),
anorexiaappetiteloss varchar(255),
bleedingvagina varchar(255),
changedate timestamp not null,
chestpain varchar(255),
comaunconscious varchar(255),
confuseddisoriented varchar(255),
conjunctivitis varchar(255),
cough varchar(255),
creationdate timestamp not null,
diarrhea varchar(255),
difficultybreathing varchar(255),
difficultyswallowing varchar(255),
digestedbloodvomit varchar(255),
epistaxis varchar(255),
eyepainlightsensitive varchar(255),
fever varchar(255),
gumsbleeding varchar(255),
headache varchar(255),
hematemesis varchar(255),
hematuria varchar(255),
hemoptysis varchar(255),
hiccups varchar(255),
injectionsitebleeding varchar(255),
intensefatigueweakness varchar(255),
jaundice varchar(255),
jointpain varchar(255),
melena varchar(255),
musclepain varchar(255),
otherhemorrhagic varchar(255),
otherhemorrhagictext varchar(255),
othernonhemorrhagic varchar(255),
othernonhemorrhagicsymptoms varchar(255),
petechiae varchar(255),
skinrash varchar(255),
sorethroat varchar(255),
onsetdate timestamp without time zone,
temperature real,
temperaturesource varchar(255),
unexplainedbleeding varchar(255),
uuid varchar(36) not null unique,
vomitingnausea varchar(255),
PRIMARY KEY (id));

ALTER TABLE symptoms OWNER TO sormas_user;

ALTER TABLE cases ADD COLUMN symptoms_id bigint;
ALTER TABLE cases ADD CONSTRAINT fk_cases_symptoms_id FOREIGN KEY (symptoms_id) REFERENCES symptoms (id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO schema_version (version_number, comment) VALUES (3, 'Symptoms');



-- 2016-10-14; #63

CREATE TABLE task (
id bigint not null,
assigneereply varchar(512),
changedate timestamp not null,
creationdate timestamp not null,
creatorcomment varchar(512),
duedate timestamp,
perceivedstart timestamp,
statuschangedate timestamp,
taskcontext varchar(255),
taskstatus varchar(255),
tasktype varchar(255),
uuid varchar(36) not null unique,
assigneeuser_id bigint,
caze_id bigint,
creatoruser_id bigint,
PRIMARY KEY (id));

ALTER TABLE task OWNER TO sormas_user;

ALTER TABLE task ADD CONSTRAINT fk_task_caze_id FOREIGN KEY (caze_id) REFERENCES cases (id);
ALTER TABLE task ADD CONSTRAINT fk_task_creatoruser_id FOREIGN KEY (creatoruser_id) REFERENCES users (id);
ALTER TABLE task ADD CONSTRAINT fk_task_assigneeuser_id FOREIGN KEY (assigneeuser_id) REFERENCES users (id);

INSERT INTO schema_version (version_number, comment) VALUES (4, 'Task');

-- 2016-10-18; #63 additions

ALTER TABLE task ADD COLUMN priority varchar(255);
ALTER TABLE task ADD COLUMN suggestedstart timestamp;
DELETE FROM task;

INSERT INTO schema_version (version_number, comment) VALUES (5, 'Task priority & suggested start');

-- 2016-10-25; #78 disease config

UPDATE cases SET disease='EVD' WHERE disease='EBOLA';

ALTER TABLE symptoms DROP COLUMN difficultyswallowing;
ALTER TABLE symptoms DROP COLUMN intensefatigueweakness;
ALTER TABLE symptoms RENAME othernonhemorrhagicsymptoms TO othernonhemorrhagicsymptomstext;
ALTER TABLE symptoms RENAME othernonhemorrhagic TO othernonhemorrhagicsymptoms;
ALTER TABLE symptoms RENAME otherhemorrhagictext TO otherhemorrhagicsymptomstext;
ALTER TABLE symptoms RENAME otherhemorrhagic TO otherhemorrhagicsymptoms;
ALTER TABLE symptoms RENAME vomitingnausea  TO vomiting;
ALTER TABLE symptoms ADD COLUMN chills character varying(255);
ALTER TABLE symptoms ADD COLUMN dehydration character varying(255);
ALTER TABLE symptoms ADD COLUMN fatigueweakness character varying(255);
ALTER TABLE symptoms ADD COLUMN highbloodpressure character varying(255);
ALTER TABLE symptoms ADD COLUMN kopliksspots character varying(255);
ALTER TABLE symptoms ADD COLUMN lethargy character varying(255);
ALTER TABLE symptoms ADD COLUMN lowbloodpressure character varying(255);
ALTER TABLE symptoms ADD COLUMN nausea character varying(255);
ALTER TABLE symptoms ADD COLUMN neckstiffness character varying(255);
ALTER TABLE symptoms ADD COLUMN oedema character varying(255);
ALTER TABLE symptoms ADD COLUMN onsetsymptom character varying(255);
ALTER TABLE symptoms ADD COLUMN otitismedia character varying(255);
ALTER TABLE symptoms ADD COLUMN refusalfeedordrink character varying(255);
ALTER TABLE symptoms ADD COLUMN runnynose character varying(255);
ALTER TABLE symptoms ADD COLUMN seizures character varying(255);
ALTER TABLE symptoms ADD COLUMN sepsis character varying(255);
ALTER TABLE symptoms ADD COLUMN swollenlymphnodes character varying(255);
ALTER TABLE symptoms ADD COLUMN symptomatic boolean;

INSERT INTO schema_version (version_number, comment) VALUES (6, 'EBOLA -> EVD; Symptoms');

-- 2016-11-08; case + user: replaced supervisor references with regional references #90

ALTER TABLE public.cases DROP COLUMN casesupervisor_id;
ALTER TABLE public.cases DROP COLUMN contactsupervisor_id;
ALTER TABLE public.cases DROP COLUMN surveillancesupervisor_id;
ALTER TABLE public.cases ADD COLUMN region_id bigint;
ALTER TABLE public.cases ADD COLUMN district_id bigint;
ALTER TABLE public.cases ADD COLUMN community_id bigint;
ALTER TABLE public.cases ADD CONSTRAINT fk_cases_region_id FOREIGN KEY (region_id) REFERENCES public.region (id) ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE public.cases ADD CONSTRAINT fk_cases_district_id FOREIGN KEY (district_id) REFERENCES public.district (id) ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE public.cases ADD CONSTRAINT fk_cases_community_id FOREIGN KEY (community_id) REFERENCES public.community (id) ON UPDATE NO ACTION ON DELETE NO ACTION;
UPDATE public.cases SET region_id=(SELECT id FROM public.region LIMIT 1);

ALTER TABLE public.users ADD COLUMN district_id bigint;
ALTER TABLE public.users ADD CONSTRAINT fk_users_district_id FOREIGN KEY (district_id) REFERENCES public.district (id) ON UPDATE NO ACTION ON DELETE NO ACTION;
UPDATE public.users SET region_id=(SELECT id FROM public.region LIMIT 1);

INSERT INTO schema_version (version_number, comment) VALUES (7, 'Case + User: replaced supervisor references with regional references');

-- 2016-11-10; Contact #85

CREATE TABLE contact (
id bigint not null,
changedate timestamp not null,
contactproximity varchar(255),
contactstatus varchar(255),
creationdate timestamp not null,
lastcontactdate timestamp,
reportdatetime timestamp not null,
uuid varchar(36) not null unique,
caze_id bigint not null,
person_id bigint not null,
reportinguser_id bigint not null,
primary key (id));

ALTER TABLE contact ADD CONSTRAINT fk_contact_person_id FOREIGN KEY (person_id) REFERENCES person (id);
ALTER TABLE contact ADD CONSTRAINT fk_contact_caze_id FOREIGN KEY (caze_id) REFERENCES cases (id);
ALTER TABLE contact ADD CONSTRAINT fk_contact_reportinguser_id FOREIGN KEY (reportinguser_id) REFERENCES users (id);

ALTER TABLE public.cases ALTER COLUMN reportdate SET NOT NULL;
ALTER TABLE public.cases ALTER COLUMN reportinguser_id SET NOT NULL;

INSERT INTO schema_version (version_number, comment) VALUES (8, 'Contact; Cases report not null');

-- 2016-11-15; Contact #85

ALTER TABLE contact ADD COLUMN description varchar(512);

INSERT INTO schema_version (version_number, comment) VALUES (9, 'Contact.description');

-- 2016-11-16; Contact #85

ALTER TABLE contact ADD COLUMN contactofficer_id bigint;
ALTER TABLE contact ADD CONSTRAINT fk_contact_contactofficer_id FOREIGN KEY (contactofficer_id) REFERENCES users (id);
ALTER TABLE contact OWNER TO sormas_user;

INSERT INTO schema_version (version_number, comment) VALUES (10, 'Contact.contactOfficer + OWNER');

-- 2016-11-29; Contact Visits backend #10

ALTER TABLE contact DROP COLUMN contactstatus;
ALTER TABLE contact ADD COLUMN contactclassification varchar(255);
ALTER TABLE contact ADD COLUMN followupstatus varchar(255);
ALTER TABLE contact ADD COLUMN followupuntil timestamp;

CREATE TABLE visit (
	id bigint not null,
	uuid varchar(36) not null unique,
	changedate timestamp not null,
	creationdate timestamp not null,
	person_id bigint not null,
	visituser_id bigint not null,
    visitremarks character varying(512),
    disease character varying(255),
	visitdatetime timestamp not null,
    visitstatus character varying(255),
	symptoms_id bigint,
	primary key (id));
ALTER TABLE visit OWNER TO sormas_user;

ALTER TABLE visit ADD CONSTRAINT fk_visit_symptoms_id FOREIGN KEY (symptoms_id) REFERENCES symptoms (id) ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE visit ADD CONSTRAINT fk_visit_person_id FOREIGN KEY (person_id) REFERENCES person (id);
ALTER TABLE visit ADD CONSTRAINT fk_visit_visituser_id FOREIGN KEY (visituser_id) REFERENCES users (id);

INSERT INTO schema_version (version_number, comment) VALUES (11, 'visit, contact classification, follow-up status, follow-up until');

-- 2016-12-15; Assign tasks to a contact #53

ALTER TABLE task ADD COLUMN contact_id bigint;
ALTER TABLE task ADD CONSTRAINT fk_task_contact_id FOREIGN KEY (contact_id) REFERENCES contact (id);
INSERT INTO schema_version (version_number, comment) VALUES (12, 'Contact added to task');

-- 2016-12-14 Split CaseStatus to CaseClassification and InvestigationStatus #40

ALTER TABLE cases ADD COLUMN caseclassification character varying(255) DEFAULT 'POSSIBLE' NOT NULL;
ALTER TABLE cases ADD COLUMN investigationstatus character varying(255) DEFAULT 'PENDING' NOT NULL;
ALTER TABLE cases DROP COLUMN casestatus;

INSERT INTO schema_version (version_number, comment) VALUES (13, 'Split CaseStatus to CaseClassification and InvestigationStatus');

-- 2016-12-16 Update symptoms #41

ALTER TABLE symptoms DROP COLUMN comaunconscious;
ALTER TABLE symptoms DROP COLUMN epistaxis;
ALTER TABLE symptoms DROP COLUMN hematemesis;
ALTER TABLE symptoms DROP COLUMN hematuria;
ALTER TABLE symptoms DROP COLUMN jaundice;
ALTER TABLE symptoms DROP COLUMN melena;
ALTER TABLE symptoms DROP COLUMN petechiae;
ALTER TABLE symptoms DROP COLUMN chills;
ALTER TABLE symptoms DROP COLUMN highbloodpressure;
ALTER TABLE symptoms DROP COLUMN lethargy;
ALTER TABLE symptoms DROP COLUMN lowbloodpressure;
ALTER TABLE symptoms DROP COLUMN oedema;
ALTER TABLE symptoms DROP COLUMN sepsis;
ALTER TABLE symptoms DROP COLUMN swollenlymphnodes;
ALTER TABLE symptoms ADD COLUMN bloodinstool character varying(255);
ALTER TABLE symptoms ADD COLUMN nosebleeding character varying(255);
ALTER TABLE symptoms ADD COLUMN bloodyblackstool character varying(255);
ALTER TABLE symptoms ADD COLUMN redbloodvomit character varying(255);
ALTER TABLE symptoms ADD COLUMN coughingblood character varying(255);
ALTER TABLE symptoms ADD COLUMN skinbruising character varying(255);
ALTER TABLE symptoms ADD COLUMN bloodurine character varying(255);
ALTER TABLE symptoms ADD COLUMN alteredconsciousness character varying(255);
ALTER TABLE symptoms ADD COLUMN throbocytopenia character varying(255);
ALTER TABLE symptoms ADD COLUMN hearingloss character varying(255);
ALTER TABLE symptoms ADD COLUMN shock character varying(255);
ALTER TABLE symptoms ADD COLUMN symptomscomments character varying(255);

INSERT INTO schema_version (version_number, comment) VALUES (14, 'Update symptoms');

-- 2017-01-05 Event backend #63

CREATE TABLE events (
	id bigint not null,
	uuid varchar(36) not null unique,
	changedate timestamp not null,
	creationdate timestamp not null,
	eventtype varchar(255) not null,
	eventstatus varchar(255) not null,
	eventdesc varchar(512) not null,
	eventdate timestamp,
	reportdatetime timestamp not null,
	reportinguser_id bigint not null,
	location_id bigint,
	typeofplace varchar(255) not null,
	srcfirstname varchar(512) not null,
	srclastname varchar(512) not null,
	srctelno varchar(512) not null,
	srcemail varchar(512),
	primary key(id));
ALTER TABLE events OWNER TO sormas_user;

ALTER TABLE events ADD CONSTRAINT fk_events_reportinguser_id FOREIGN KEY (reportinguser_id) REFERENCES users (id);
ALTER TABLE events ADD CONSTRAINT fk_events_location_id FOREIGN KEY (location_id) REFERENCES location (id);

CREATE TABLE eventparticipant (
	id bigint not null,
	uuid varchar(36) not null unique,
	changedate timestamp not null,
	creationdate timestamp not null,
	event_id bigint,
	person_id bigint,
	kindofinvolvement varchar(255),
	primary key(id));
ALTER TABLE eventparticipant OWNER TO sormas_user;

ALTER TABLE eventparticipant ADD CONSTRAINT fk_eventparticipant_event_id FOREIGN KEY (event_id) REFERENCES events (id);
ALTER TABLE eventparticipant ADD CONSTRAINT fk_eventparticipant_person_id FOREIGN KEY (person_id) REFERENCES person (id);

INSERT INTO schema_version (version_number, comment) VALUES (15, 'events, eventparticipant');

-- 2017-01-06 Renaming #63

ALTER TABLE events RENAME location_id TO eventlocation_id;

INSERT INTO schema_version (version_number, comment) VALUES (16, 'renamed eventlocation');

-- 2017-01-10 Update events #63

ALTER TABLE events ADD COLUMN disease character varying(255);
ALTER TABLE events ADD COLUMN surveillanceofficer_id bigint;

INSERT INTO schema_version (version_number, comment) VALUES (17, 'update events');

-- 2017-01-11 Update events with type of place text #63

ALTER TABLE events ADD COLUMN typeofplacetext character varying(255);

INSERT INTO schema_version (version_number, comment) VALUES (18, 'update events with type of place text');

-- 2017-01-12 Assign tasks to an event #65

ALTER TABLE task ADD COLUMN event_id bigint;
ALTER TABLE task ADD CONSTRAINT fk_task_event_id FOREIGN KEY (event_id) REFERENCES events (id);

INSERT INTO schema_version (version_number, comment) VALUES (19, 'Event added to task');

-- 2017-01-17 Change kind of involvement to involvement description #66

ALTER TABLE eventparticipant DROP COLUMN kindofinvolvement;
ALTER TABLE eventparticipant ADD COLUMN involvementdescription varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (20, 'Involvement description instead of Kind of involvement');

-- 2017-01-20 Add relation to case to contact #75

ALTER TABLE contact ADD COLUMN relationtocase varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (21, 'Add relation to case to contact');

-- 2017-01-23 Add nickname and mother's maiden name to person #19

ALTER TABLE person ADD COLUMN nickname varchar(255);
ALTER TABLE person ADD COLUMN mothersmaidenname varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (22, 'Add nickname and mothers maiden name to person');

-- 2017-01-26 add health facility to user (informant) #49

ALTER TABLE users ADD COLUMN healthfacility_id bigint;
ALTER TABLE users ADD CONSTRAINT fk_users_healthfacility_id FOREIGN KEY (healthfacility_id) REFERENCES facility(id);

INSERT INTO schema_version (version_number, comment) VALUES (23, 'Add health facility to users (informant)');

-- 2017-01-30 Sample and SampleTest backend #106

CREATE TABLE samples(
	id bigint not null,
	uuid varchar(36) not null unique,
	changedate timestamp not null,
	creationdate timestamp not null,
	associatedcase_id bigint not null,
	samplecode varchar(512),
	sampledatetime timestamp not null,
	reportdatetime timestamp not null,
	reportinguser_id bigint not null,
	samplematerial varchar(255) not null,
	samplematerialtext varchar(512),
	lab_id bigint not null,
	otherlab_id bigint,
	shipmentstatus varchar(255) not null,
	shipmentdate timestamp not null,
	shipmentdetails varchar(512),
	receiveddate timestamp,
	notestpossible boolean,
	notestpossiblereason varchar(512),
	primary key(id));

CREATE TABLE sampletest(
	id bigint not null,
	uuid varchar(36) not null unique,
	changedate timestamp not null,
	creationdate timestamp not null,
	sample_id bigint not null,
	testtype varchar(255) not null,
	testtypetext varchar(512),
	testdatetime timestamp not null,
	lab_id bigint not null,
	labuser_id bigint not null,
	testresult varchar(255) not null,
	testresulttext varchar(512) not null,
	testresultverified boolean not null,
	primary key(id));

ALTER TABLE samples OWNER TO sormas_user;
ALTER TABLE sampletest OWNER TO sormas_user;

INSERT INTO schema_version (version_number, comment) VALUES (24, 'Sample and SampleTest backend');

-- 2017-02-03 Foreign keys for samples and sampletests #106

ALTER TABLE samples ADD CONSTRAINT fk_samples_associatedcase_id FOREIGN KEY (associatedcase_id) REFERENCES cases (id);
ALTER TABLE samples ADD CONSTRAINT fk_samples_reportinguser_id FOREIGN KEY (reportinguser_id) REFERENCES users (id);
ALTER TABLE samples ADD CONSTRAINT fk_samples_lab_id FOREIGN KEY (lab_id) REFERENCES facility (id);
ALTER TABLE samples ADD CONSTRAINT fk_samples_otherlab_id FOREIGN KEY (otherlab_id) REFERENCES facility (id);
ALTER TABLE sampletest ADD CONSTRAINT fk_sampletest_sample_id FOREIGN KEY (sample_id) REFERENCES samples (id);
ALTER TABLE sampletest ADD CONSTRAINT fk_sampletest_lab_id FOREIGN KEY (lab_id) REFERENCES facility (id);
ALTER TABLE sampletest ADD CONSTRAINT fk_sampletest_labuser_id FOREIGN KEY (labuser_id) REFERENCES users (id);

INSERT INTO schema_version (version_number, comment) VALUES (25, 'Foreign keys for samples and sampletest');

-- 2017-02-03 Laboratory user role #111

ALTER TABLE users ADD COLUMN laboratory_id bigint;
ALTER TABLE users ADD CONSTRAINT fk_users_laboratory_id FOREIGN KEY (laboratory_id) REFERENCES facility (id);

INSERT INTO schema_version (version_number, comment) VALUES (26, 'Laboratory user role');

-- 2017-02-03 Add admin role to user 'admin'

INSERT INTO userroles (user_id, userrole) SELECT id, 'ADMIN' FROM users WHERE username = 'admin';

INSERT INTO schema_version (version_number, comment) VALUES (27, 'Add admin role to user admin');

-- 2017-02-08 Removed null constraint from shipment date

ALTER TABLE samples ALTER COLUMN shipmentdate DROP NOT NULL;

INSERT INTO schema_version (version_number, comment) VALUES (28, 'Removed null constraint from shipment date');

-- 2017-02-17 Update samples entity #117

ALTER TABLE samples ADD COLUMN comment varchar(512);
ALTER TABLE samples DROP COLUMN notestpossible;
ALTER TABLE samples ADD COLUMN specimencondition varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (29, 'Update samples entity');

-- 2017-02-21 Hospitalization and PreviousHospitalization entities #89

CREATE TABLE hospitalization(
	id bigint not null,
	uuid varchar(36) not null unique,
	changedate timestamp not null,
	creationdate timestamp not null,
	hospitalized varchar(255),
	admissiondate timestamp,
	healthfacility_id bigint,
	isolated varchar(255),
	isolationdate timestamp,
	hospitalizedpreviously varchar(255),
	primary key(id));
ALTER TABLE hospitalization OWNER TO sormas_user;
ALTER TABLE hospitalization ADD CONSTRAINT fk_hospitalization_healthfacility_id FOREIGN KEY (healthfacility_id) REFERENCES facility (id);

CREATE TABLE previoushospitalization(
	id bigint not null,
	uuid varchar(36) not null unique,
	changedate timestamp not null,
	creationdate timestamp not null,
	admissiondate timestamp,
	dischargedate timestamp,
	healthfacility_id bigint,
	isolated varchar(255),
	hospitalization_id bigint not null,
	primary key(id));
ALTER TABLE previoushospitalization OWNER TO sormas_user;
ALTER TABLE previoushospitalization ADD CONSTRAINT fk_previoushospitalization_healthfacility_id FOREIGN KEY (healthfacility_id) REFERENCES facility (id);
ALTER TABLE previoushospitalization ADD CONSTRAINT fk_previoushospitalization_hospitalization_id FOREIGN KEY (hospitalization_id) REFERENCES hospitalization (id);

ALTER TABLE cases ADD COLUMN hospitalization_id bigint;
ALTER TABLE cases ADD CONSTRAINT fk_cases_hospitalization_id FOREIGN KEY (hospitalization_id) REFERENCES hospitalization (id);

INSERT INTO schema_version (version_number, comment) VALUES (30, 'Hospitalization and PreviousHospitalization entities');

-- 2017-02-21 Drop Abia state #28 (Kano)

DELETE FROM task;
DELETE FROM sampletest;
DELETE FROM samples;
DELETE FROM visit;
DELETE FROM contact;
DELETE FROM eventparticipant;
DELETE FROM events;
DELETE FROM cases;
DELETE FROM symptoms;
DELETE FROM person;
DELETE FROM userroles;
DELETE FROM users;
DELETE FROM facility;
DELETE FROM location;
DELETE FROM community;
DELETE FROM district;
DELETE FROM region;

INSERT INTO schema_version (version_number, comment) VALUES (31, 'Removed Abia state demo data');

-- 2017-02-24 Update hospitalization and previoushospitalization entities

ALTER TABLE hospitalization DROP COLUMN hospitalized;
ALTER TABLE hospitalization ADD COLUMN dischargedate timestamp;
ALTER TABLE previoushospitalization ADD COLUMN description varchar(512);

INSERT INTO schema_version (version_number, comment) VALUES (32, 'Update hospitalization and previoushospitalization entities');

-- 2017-02-24 Removed health facility from hospitalization

ALTER TABLE public.hospitalization DROP COLUMN healthfacility_id;
INSERT INTO schema_version (version_number, comment) VALUES (33, 'Removed health facility from hospitalization');

-- 2017-02-28 Laboratories update

DELETE FROM facility WHERE type = 'Laboratory';
INSERT INTO schema_version (version_number, comment) VALUES (34, 'Laboratories update');

-- 2017-03-01 Case measles additions

ALTER TABLE cases ADD COLUMN pregnant varchar(255);
ALTER TABLE cases ADD COLUMN measlesVaccination varchar(255);
ALTER TABLE cases ADD COLUMN measlesDoses varchar(512);
ALTER TABLE cases ADD COLUMN measlesVaccinationInfoSource varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (35, 'Case measles addition');

-- 2017-03-02 Epidemiological data

CREATE TABLE epidata(
	id bigint not null,
	uuid varchar(36) not null unique,
	changedate timestamp not null,
	creationdate timestamp not null,
	rodents varchar(255),
	bats varchar(255),
	primates varchar(255),
	swine varchar(255),
	birds varchar(255),
	poultryeat varchar(255),
	poultry varchar(255),
	poultrydetails varchar(512),
	poultrysick varchar(255),
	poultrysickdetails varchar(512),
	poultrydate timestamp,
	poultrylocation varchar(512),
	wildbirds varchar(255),
	wildbirdsdetails varchar(512),
	wildbirdsdate timestamp,
	wildbirdslocation varchar(512),
	cattle varchar(255),
	otheranimals varchar(255),
	otheranimalsdetails varchar(512),
	watersource varchar(255),
	watersourceother varchar(512),
	waterbody varchar(255),
	waterbodydetails varchar(512),
	tickbite varchar(255),
	burialattended varchar(255),
	gatheringattended varchar(255),
	traveled varchar(255),
	primary key(id)
);
ALTER TABLE epidata OWNER TO sormas_user;

CREATE TABLE epidataburial(
	id bigint not null,
	uuid varchar(36) not null unique,
	changedate timestamp not null,
	creationdate timestamp not null,
	epidata_id bigint not null,
	burialpersonname varchar(512),
	burialrelation varchar(512),
	burialdatefrom timestamp,
	burialdateto timestamp,
	burialaddress_id bigint,
	burialill varchar(255),
	burialtouching varchar(255),
	primary key(id)
);
ALTER TABLE epidataburial OWNER TO sormas_user;
ALTER TABLE epidataburial ADD CONSTRAINT fk_epidataburial_epidata_id FOREIGN KEY (epidata_id) REFERENCES epidata(id);
ALTER TABLE epidataburial ADD CONSTRAINT fk_epidataburial_burialaddress_id FOREIGN KEY (burialaddress_id) REFERENCES location(id);

CREATE TABLE epidatagathering(
	id bigint not null,
	uuid varchar(36) not null unique,
	changedate timestamp not null,
	creationdate timestamp not null,
	epidata_id bigint not null,
	description varchar(512),
	gatheringdate timestamp,
	gatheringaddress_id bigint,
	primary key(id)
);
ALTER TABLE epidatagathering OWNER TO sormas_user;
ALTER TABLE epidatagathering ADD CONSTRAINT fk_epidatagathering_epidata_id FOREIGN KEY (epidata_id) REFERENCES epidata(id);
ALTER TABLE epidatagathering ADD CONSTRAINT fk_epidatagathering_gatheringaddress_id FOREIGN KEY (gatheringaddress_id) REFERENCES location(id);

CREATE TABLE epidatatravel(
	id bigint not null,
	uuid varchar(36) not null unique,
	changedate timestamp not null,
	creationdate timestamp not null,
	epidata_id bigint not null,
	traveltype varchar(255),
	traveldestination varchar(512),
	traveldatefrom timestamp,
	traveldateto timestamp,
	primary key(id)
);
ALTER TABLE epidatatravel OWNER TO sormas_user;
ALTER TABLE epidatatravel ADD CONSTRAINT fk_epidatatravel_epidata_id FOREIGN KEY (epidata_id) REFERENCES epidata(id);

ALTER TABLE cases ADD COLUMN epidata_id bigint;
ALTER TABLE cases ADD CONSTRAINT fk_cases_epidata_id FOREIGN KEY (epidata_id) REFERENCES epidata(id);

INSERT INTO schema_version (version_number, comment) VALUES (36, 'Epidemiological data');


CREATE TEMP TABLE tmp_caseids AS SELECT cases.id AS caseid, nextval('entity_seq') AS epiid FROM cases WHERE cases.epidata_id IS NULL;
INSERT INTO epidata (id, changedate, creationdate, uuid)
	SELECT epiid, now(), now(), uuid_in(CAST(md5(CAST(random() AS text) || CAST(clock_timestamp() AS text)) AS cstring)) FROM tmp_caseids;
UPDATE cases SET epidata_id = epiid FROM tmp_caseids t where cases.id = t.caseid;
DROP TABLE tmp_caseids;

INSERT INTO schema_version (version_number, comment) VALUES (37, 'Epidemiological data: added empty entites');

-- 2017-03-02 Don't use location entity for facilities #138

ALTER TABLE facility DROP COLUMN location_id;
ALTER TABLE facility ADD COLUMN region_id bigint;
ALTER TABLE facility ADD COLUMN district_id bigint;
ALTER TABLE facility ADD COLUMN community_id bigint;
ALTER TABLE facility ADD COLUMN city varchar(512);
ALTER TABLE facility ADD COLUMN latitude double precision;
ALTER TABLE facility ADD COLUMN longitude double precision;

ALTER TABLE facility ADD CONSTRAINT fk_facility_region_id FOREIGN KEY (region_id) REFERENCES region(id);
ALTER TABLE facility ADD CONSTRAINT fk_facility_district_id FOREIGN KEY (district_id) REFERENCES district(id);
ALTER TABLE facility ADD CONSTRAINT fk_facility_community_id FOREIGN KEY (community_id) REFERENCES community(id);

DELETE FROM task;
DELETE FROM sampletest;
DELETE FROM samples;
DELETE FROM visit;
DELETE FROM contact;
DELETE FROM eventparticipant;
DELETE FROM events;
DELETE FROM cases;
DELETE FROM symptoms;
DELETE FROM person;
DELETE FROM userroles;
DELETE FROM users;
DELETE FROM previoushospitalization;
DELETE FROM hospitalization;
DELETE FROM facility;
DELETE FROM epidataburial;
DELETE FROM epidatagathering;
DELETE FROM epidatatravel;
DELETE FROM epidata;
DELETE FROM location;
DELETE FROM community;
DELETE FROM district;
DELETE FROM region;

INSERT INTO schema_version (version_number, comment) VALUES (38, 'Dont use location entity for facilities');

-- 2017-03-22 Split sample code and lab sample ID #155

ALTER TABLE samples ADD COLUMN labsampleid varchar(512);

INSERT INTO schema_version (version_number, comment) VALUES (39, 'Split sample code and lab sample ID');

-- 2017-03-22 Drop not null constraint from sample test result text

ALTER TABLE sampletest ALTER COLUMN testresulttext DROP NOT NULL;

INSERT INTO schema_version (version_number, comment) VALUES (40, 'Drop not null constraint from sample test result text');

-- 2017-03-28 Added EPID number to case #167

ALTER TABLE cases ADD COLUMN epidnumber varchar(512);

INSERT INTO schema_version (version_number, comment) VALUES (41, 'Added EPID number to case');

-- 2017-03-29 Task types update, drop all tasks #97

DELETE FROM task;

INSERT INTO schema_version (version_number, comment) VALUES (42, 'Task types update, drop all tasks');

-- 2017-04-05 Rename POSSIBLE case classification to NOT_CLASSIFIED #175

UPDATE cases SET caseclassification = 'NOT_CLASSIFIED' WHERE caseclassification = 'POSSIBLE';

INSERT INTO schema_version (version_number, comment) VALUES (43, 'Rename POSSBILE case classification to NOT_CLASSIFIED');

-- 2017-04-05 #176

UPDATE task SET taskstatus = 'REMOVED' WHERE taskstatus = 'DISCARDED';

INSERT INTO schema_version (version_number, comment) VALUES (44, 'Rename DISCARDED task status to REMOVED');

-- 2017-04-07 Sample soruce and suggested type of test #178

ALTER TABLE samples ADD COLUMN samplesource varchar(255);
ALTER TABLE samples ADD COLUMN suggestedtypeoftest varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (45, 'Sample source and suggested type of test');

-- 2017-05-17 Missing person fields #196

ALTER TABLE person DROP COLUMN dead;
ALTER TABLE person DROP COLUMN buriallocation_id;
ALTER TABLE person DROP COLUMN deathlocation_id;
ALTER TABLE person ADD COLUMN deathplacetype varchar(255);
ALTER TABLE person ADD COLUMN deathplacedescription varchar(512);
ALTER TABLE person ADD COLUMN burialplacedescription varchar(512);

INSERT INTO schema_version (version_number, comment) VALUES (46, 'Missing person fields #196');

-- 2017-05-18 IllLocation for symptoms #196

ALTER TABLE cases DROP COLUMN illlocation_id;
ALTER TABLE symptoms ADD COLUMN illlocation_id bigint;
ALTER TABLE symptoms ADD COLUMN illlocationfrom timestamp;
ALTER TABLE symptoms ADD COLUMN illlocationto timestamp;

ALTER TABLE symptoms ADD CONSTRAINT fk_symptoms_illlocation_id FOREIGN KEY (illlocation_id) REFERENCES location(id);

INSERT INTO schema_version (version_number, comment) VALUES (47, 'IllLocation for symptoms');

-- 2017-06-07 change date for embedded lists #221

ALTER TABLE epidata ADD COLUMN changedateofembeddedlists timestamp without time zone;
ALTER TABLE hospitalization ADD COLUMN changedateofembeddedlists timestamp without time zone;

INSERT INTO schema_version (version_number, comment) VALUES (48, 'Change data for embedded lists (epidata and hospitalization');

-- 2017-06-20 data history for future reporting (postgres temporal tables) #170

ALTER TABLE cases ADD COLUMN sys_period tstzrange;
UPDATE cases SET sys_period=tstzrange(creationdate, null);
ALTER TABLE cases ALTER COLUMN sys_period SET NOT NULL;
CREATE TABLE cases_history (LIKE cases);
CREATE TRIGGER versioning_trigger
BEFORE INSERT OR UPDATE OR DELETE ON cases
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'cases_history', true);
ALTER TABLE cases_history OWNER TO sormas_user;

ALTER TABLE contact ADD COLUMN sys_period tstzrange;
UPDATE contact SET sys_period=tstzrange(creationdate, null);
ALTER TABLE contact ALTER COLUMN sys_period SET NOT NULL;
CREATE TABLE contact_history (LIKE contact);
CREATE TRIGGER versioning_trigger
BEFORE INSERT OR UPDATE OR DELETE ON contact
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'contact_history', true);
ALTER TABLE contact_history OWNER TO sormas_user;

ALTER TABLE epidata ADD COLUMN sys_period tstzrange;
UPDATE epidata SET sys_period=tstzrange(creationdate, null);
ALTER TABLE epidata ALTER COLUMN sys_period SET NOT NULL;
CREATE TABLE epidata_history (LIKE epidata);
CREATE TRIGGER versioning_trigger
BEFORE INSERT OR UPDATE OR DELETE ON epidata
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'epidata_history', true);
ALTER TABLE epidata_history OWNER TO sormas_user;

ALTER TABLE epidataburial ADD COLUMN sys_period tstzrange;
UPDATE epidataburial SET sys_period=tstzrange(creationdate, null);
ALTER TABLE epidataburial ALTER COLUMN sys_period SET NOT NULL;
CREATE TABLE epidataburial_history (LIKE epidataburial);
CREATE TRIGGER versioning_trigger
BEFORE INSERT OR UPDATE OR DELETE ON epidataburial
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'epidataburial_history', true);
ALTER TABLE epidataburial_history OWNER TO sormas_user;

ALTER TABLE epidatagathering ADD COLUMN sys_period tstzrange;
UPDATE epidatagathering SET sys_period=tstzrange(creationdate, null);
ALTER TABLE epidatagathering ALTER COLUMN sys_period SET NOT NULL;
CREATE TABLE epidatagathering_history (LIKE epidatagathering);
CREATE TRIGGER versioning_trigger
BEFORE INSERT OR UPDATE OR DELETE ON epidatagathering
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'epidatagathering_history', true);
ALTER TABLE epidatagathering_history OWNER TO sormas_user;

ALTER TABLE epidatatravel ADD COLUMN sys_period tstzrange;
UPDATE epidatatravel SET sys_period=tstzrange(creationdate, null);
ALTER TABLE epidatatravel ALTER COLUMN sys_period SET NOT NULL;
CREATE TABLE epidatatravel_history (LIKE epidatatravel);
CREATE TRIGGER versioning_trigger
BEFORE INSERT OR UPDATE OR DELETE ON epidatatravel
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'epidatatravel_history', true);
ALTER TABLE epidatatravel_history OWNER TO sormas_user;

ALTER TABLE events ADD COLUMN sys_period tstzrange;
UPDATE events SET sys_period=tstzrange(creationdate, null);
ALTER TABLE events ALTER COLUMN sys_period SET NOT NULL;
CREATE TABLE events_history (LIKE events);
CREATE TRIGGER versioning_trigger
BEFORE INSERT OR UPDATE OR DELETE ON events
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'events_history', true);
ALTER TABLE events_history OWNER TO sormas_user;

ALTER TABLE location ADD COLUMN sys_period tstzrange;
UPDATE location SET sys_period=tstzrange(creationdate, null);
ALTER TABLE location ALTER COLUMN sys_period SET NOT NULL;
CREATE TABLE location_history (LIKE location);
CREATE TRIGGER versioning_trigger
BEFORE INSERT OR UPDATE OR DELETE ON location
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'location_history', true);
ALTER TABLE location_history OWNER TO sormas_user;

ALTER TABLE person ADD COLUMN sys_period tstzrange;
UPDATE person SET sys_period=tstzrange(creationdate, null);
ALTER TABLE person ALTER COLUMN sys_period SET NOT NULL;
CREATE TABLE person_history (LIKE person);
CREATE TRIGGER versioning_trigger
BEFORE INSERT OR UPDATE OR DELETE ON person
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'person_history', true);
ALTER TABLE person_history OWNER TO sormas_user;

ALTER TABLE previoushospitalization ADD COLUMN sys_period tstzrange;
UPDATE previoushospitalization SET sys_period=tstzrange(creationdate, null);
ALTER TABLE previoushospitalization ALTER COLUMN sys_period SET NOT NULL;
CREATE TABLE previoushospitalization_history (LIKE previoushospitalization);
CREATE TRIGGER versioning_trigger
BEFORE INSERT OR UPDATE OR DELETE ON previoushospitalization
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'previoushospitalization_history', true);
ALTER TABLE previoushospitalization_history OWNER TO sormas_user;

ALTER TABLE samples ADD COLUMN sys_period tstzrange;
UPDATE samples SET sys_period=tstzrange(creationdate, null);
ALTER TABLE samples ALTER COLUMN sys_period SET NOT NULL;
CREATE TABLE samples_history (LIKE samples);
CREATE TRIGGER versioning_trigger
BEFORE INSERT OR UPDATE OR DELETE ON samples
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'samples_history', true);
ALTER TABLE samples_history OWNER TO sormas_user;

ALTER TABLE sampletest ADD COLUMN sys_period tstzrange;
UPDATE sampletest SET sys_period=tstzrange(creationdate, null);
ALTER TABLE sampletest ALTER COLUMN sys_period SET NOT NULL;
CREATE TABLE sampletest_history (LIKE sampletest);
CREATE TRIGGER versioning_trigger
BEFORE INSERT OR UPDATE OR DELETE ON sampletest
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'sampletest_history', true);
ALTER TABLE sampletest_history OWNER TO sormas_user;

ALTER TABLE symptoms ADD COLUMN sys_period tstzrange;
UPDATE symptoms SET sys_period=tstzrange(creationdate, null);
ALTER TABLE symptoms ALTER COLUMN sys_period SET NOT NULL;
CREATE TABLE symptoms_history (LIKE symptoms);
CREATE TRIGGER versioning_trigger
BEFORE INSERT OR UPDATE OR DELETE ON symptoms
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'symptoms_history', true);
ALTER TABLE symptoms_history OWNER TO sormas_user;

ALTER TABLE task ADD COLUMN sys_period tstzrange;
UPDATE task SET sys_period=tstzrange(creationdate, null);
ALTER TABLE task ALTER COLUMN sys_period SET NOT NULL;
CREATE TABLE task_history (LIKE task);
CREATE TRIGGER versioning_trigger
BEFORE INSERT OR UPDATE OR DELETE ON task
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'task_history', true);
ALTER TABLE task_history OWNER TO sormas_user;

ALTER TABLE users ADD COLUMN sys_period tstzrange;
UPDATE users SET sys_period=tstzrange(creationdate, null);
ALTER TABLE users ALTER COLUMN sys_period SET NOT NULL;
CREATE TABLE users_history (LIKE users);
CREATE TRIGGER versioning_trigger
BEFORE INSERT OR UPDATE OR DELETE ON users
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'users_history', true);
ALTER TABLE users_history OWNER TO sormas_user;

ALTER TABLE visit ADD COLUMN sys_period tstzrange;
UPDATE visit SET sys_period=tstzrange(creationdate, null);
ALTER TABLE visit ALTER COLUMN sys_period SET NOT NULL;
CREATE TABLE visit_history (LIKE visit);
CREATE TRIGGER versioning_trigger
BEFORE INSERT OR UPDATE OR DELETE ON visit
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'visit_history', true);
ALTER TABLE visit_history OWNER TO sormas_user;

INSERT INTO schema_version (version_number, comment) VALUES (49, 'data history for future reporting (postgres temporal tables) #170');

-- 2017-07-19 other health facility description for cases #238
ALTER TABLE cases ADD COLUMN healthfacilitydetails varchar(512);

-- 2017-07-20 Database wipe for new infrastructure data #237
BEGIN;
DELETE FROM task;
DELETE FROM sampletest;
DELETE FROM samples;
DELETE FROM visit;
DELETE FROM contact;
DELETE FROM eventparticipant;
DELETE FROM events;
DELETE FROM cases;
DELETE FROM symptoms;
DELETE FROM person;
DELETE FROM previoushospitalization;
DELETE FROM hospitalization;
DELETE FROM epidataburial;
DELETE FROM epidatagathering;
DELETE FROM epidatatravel;
DELETE FROM epidata;
DELETE FROM userroles;
DELETE FROM users;
DELETE FROM location;
DELETE FROM facility;
DELETE FROM community;
DELETE FROM district;
DELETE FROM region;
COMMIT;

INSERT INTO schema_version (version_number, comment) VALUES (50, 'other health facility description for cases #238');

-- 2015-07-25 fix wrong ending in LGA names of state Oyo. Will not be sent to mobile devices unless reinstalled
UPDATE public.district SET name=replace(name,' LGA', '') WHERE name ~ ' LGA$';
INSERT INTO schema_version (version_number, comment) VALUES (51, 'Fix Oyo LGA names #230');

-- 2017-07-25 Country, state and LGA codes #230
ALTER TABLE district ADD COLUMN epidcode varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (52, 'Country, state and LGA codes');

-- 2017-07-26 Update EPID codes #230

UPDATE district SET epidcode = null;

INSERT INTO schema_version (version_number, comment) VALUES (53, 'Update EPID codes');

-- 2017-07-31 Split EPID code #230
ALTER TABLE region ADD COLUMN epidcode varchar(255);
UPDATE public.district SET epidcode=substr(epidcode, 9);
UPDATE public.district SET changedate=now();
UPDATE public.region SET changedate=now();

INSERT INTO schema_version (version_number, comment) VALUES (54, 'Split EPID code');

-- 2017-08-07 Remove contact officer from case #267
ALTER TABLE cases DROP COLUMN contactofficer_id;

INSERT INTO schema_version (version_number, comment) VALUES (55, 'Remove contact officer from case');

-- 2017-08-07 Add referredTo field to sample #255
ALTER TABLE samples ADD COLUMN referredto_id bigint;
ALTER TABLE samples ADD CONSTRAINT fk_samples_referredto_id FOREIGN KEY (referredto_id) REFERENCES samples (id);

INSERT INTO schema_version (version_number, comment) VALUES (56, 'Add referredTo field to sample');

-- 2017-08-10 Replace ShipmentStatus with shipped and received booleans #229
ALTER TABLE samples ADD COLUMN shipped boolean;
ALTER TABLE samples ADD COLUMN received boolean;
UPDATE samples SET shipped=true WHERE shipmentstatus = 'SHIPPED' OR shipmentstatus = 'RECEIVED' OR shipmentstatus = 'REFERRED_OTHER_LAB';
UPDATE samples SET received=true WHERE shipmentstatus = 'RECEIVED' OR shipmentstatus = 'REFERRED_OTHER_LAB';
UPDATE samples SET shipped=false WHERE shipmentstatus = 'NOT_SHIPPED';
UPDATE samples SET received=false WHERE shipmentstatus = 'NOT_SHIPPED' OR shipmentstatus = 'SHIPPED';
ALTER TABLE samples DROP COLUMN shipmentstatus;
ALTER TABLE samples_history DROP COLUMN shipmentstatus;
ALTER TABLE cases_history DROP COLUMN contactofficer_id;

INSERT INTO schema_version (version_number, comment) VALUES (57, 'Replace ShipmentStatus with shipped and received booleans');

-- 2017-08-16 Add GEO tags to cases, contacts, events, visits and tasks #86
ALTER TABLE cases ADD COLUMN reportlat double precision;
ALTER TABLE cases ADD COLUMN reportlon double precision;
ALTER TABLE contact ADD COLUMN reportlat double precision;
ALTER TABLE contact ADD COLUMN reportlon double precision;
ALTER TABLE events ADD COLUMN reportlat double precision;
ALTER TABLE events ADD COLUMN reportlon double precision;
ALTER TABLE visit ADD COLUMN reportlat double precision;
ALTER TABLE visit ADD COLUMN reportlon double precision;
ALTER TABLE task ADD COLUMN closedlat double precision;
ALTER TABLE task ADD COLUMN closedlon double precision;

INSERT INTO schema_version (version_number, comment) VALUES (58, 'Add GEO tags to cases, contacts, events, visits and tasks');

-- 2017-08-16 Add GEO tags to cases, contacts, events, visits and tasks #86
ALTER TABLE cases_history ADD COLUMN reportlat double precision;
ALTER TABLE cases_history ADD COLUMN reportlon double precision;
ALTER TABLE contact_history ADD COLUMN reportlat double precision;
ALTER TABLE contact_history ADD COLUMN reportlon double precision;
ALTER TABLE events_history ADD COLUMN reportlat double precision;
ALTER TABLE events_history ADD COLUMN reportlon double precision;
ALTER TABLE visit_history ADD COLUMN reportlat double precision;
ALTER TABLE visit_history ADD COLUMN reportlon double precision;
ALTER TABLE task_history ADD COLUMN closedlat double precision;
ALTER TABLE task_history ADD COLUMN closedlon double precision;

INSERT INTO schema_version (version_number, comment) VALUES (59, 'Add GEO tags to history tables of cases, contacts, events, visits and tasks');

-- 2017-08-28 Follow-up comment #70
ALTER TABLE contact ADD COLUMN followUpComment varchar(512);

INSERT INTO schema_version (version_number, comment) VALUES (60, 'Follow-up comment #70');

-- 2017-08-31 Rename 'Other' and 'None' health facilities #261
UPDATE facility SET name = 'Other health facility' WHERE uuid = 'SORMAS-CONSTID-OTHERS-FACILITY';
UPDATE facility SET name = 'Not a health facility' WHERE uuid = 'SORMAS-CONSTID-ISNONE-FACILITY';

INSERT INTO schema_version (version_number, comment) VALUES (61, 'Rename "Other" and "None" health facilities');

-- 2017-09-04 RDC for Previous Hospitalizations #104
ALTER TABLE previoushospitalization ADD COLUMN region_id bigint;
ALTER TABLE previoushospitalization ADD COLUMN district_id bigint;
ALTER TABLE previoushospitalization ADD COLUMN community_id bigint;

ALTER TABLE previoushospitalization ADD CONSTRAINT fk_previoushospitalization_region_id FOREIGN KEY (region_id) REFERENCES region (id);
ALTER TABLE previoushospitalization ADD CONSTRAINT fk_previoushospitalization_district_id FOREIGN KEY (district_id) REFERENCES district (id);
ALTER TABLE previoushospitalization ADD CONSTRAINT fk_previoushospitalization_community_id FOREIGN KEY (community_id) REFERENCES community (id);

ALTER TABLE previoushospitalization_history ADD COLUMN region_id bigint;
ALTER TABLE previoushospitalization_history ADD COLUMN district_id bigint;
ALTER TABLE previoushospitalization_history ADD COLUMN community_id bigint;

UPDATE previoushospitalization SET region_id = (SELECT region_id FROM facility WHERE facility.id = previoushospitalization.healthfacility_id);
UPDATE previoushospitalization SET district_id = (SELECT district_id FROM facility WHERE facility.id = previoushospitalization.healthfacility_id);
UPDATE previoushospitalization SET community_id = (SELECT community_id FROM facility WHERE facility.id = previoushospitalization.healthfacility_id);
-- Set region, district and community to the values of the case for 'Other' and 'None' health facilities
UPDATE previoushospitalization SET region_id = (SELECT region_id FROM cases WHERE cases.hospitalization_id = previoushospitalization.hospitalization_id) WHERE region_id IS NULL;
UPDATE previoushospitalization SET district_id = (SELECT district_id FROM cases WHERE cases.hospitalization_id = previoushospitalization.hospitalization_id) WHERE district_id IS NULL;
UPDATE previoushospitalization SET community_id = (SELECT community_id FROM cases WHERE cases.hospitalization_id = previoushospitalization.hospitalization_id) WHERE community_id IS NULL;

INSERT INTO schema_version (version_number, comment) VALUES (62, 'RDC for Previous Hospitalizations');

-- 2017-09-05 Generic names for 'Other' and 'None' facilities #261
UPDATE facility SET name = 'OTHER_FACILITY' WHERE uuid = 'SORMAS-CONSTID-OTHERS-FACILITY';
UPDATE facility SET name = 'NO_FACILITY' WHERE uuid = 'SORMAS-CONSTID-ISNONE-FACILITY';

INSERT INTO schema_version (version_number, comment) VALUES (63, 'Generic names for Other and None facilities');

-- 2017-09-06 Weekly Reports and Weekly Report Entries #171
CREATE TABLE weeklyreport(
	id bigint not null,
	uuid varchar(36) not null unique,
	changedate timestamp not null,
	creationdate timestamp not null,
	healthfacility_id bigint not null,
	informant_id bigint not null,
	reportdatetime timestamp not null,
	totalnumberofcases integer not null,
	primary key(id)
);

ALTER TABLE weeklyreport OWNER TO sormas_user;
ALTER TABLE weeklyreport ADD CONSTRAINT fk_weeklyreport_healthfacility_id FOREIGN KEY (healthfacility_id) REFERENCES facility(id);
ALTER TABLE weeklyreport ADD CONSTRAINT fk_weeklyreport_informant_id FOREIGN KEY (informant_id) REFERENCES users(id);

CREATE TABLE weeklyreportentry(
	id bigint not null,
	uuid varchar(36) not null unique,
	changedate timestamp not null,
	creationdate timestamp not null,
	weeklyreport_id bigint not null,
    disease character varying(255) not null,
    numberofcases integer not null,
    primary key(id)
);

ALTER TABLE weeklyreportentry OWNER TO sormas_user;
ALTER TABLE weeklyreportentry ADD CONSTRAINT fk_weeklyreportentry_weeklyreport_id FOREIGN KEY (weeklyreport_id) REFERENCES weeklyreport(id);

INSERT INTO schema_version (version_number, comment) VALUES (64, 'Weekly Reports and Weekly Report Entries');

-- 2017-09-12 Year and epi week for Weekly Reports #171
ALTER TABLE weeklyreport ADD COLUMN year integer not null;
ALTER TABLE weeklyreport ADD COLUMN epiweek integer not null;

INSERT INTO schema_version (version_number, comment) VALUES (65, 'Year and epi week for Weekly Reports');

-- 2017-09-12 Population for regions #82
ALTER TABLE region ADD COLUMN population integer;
ALTER TABLE region ADD COLUMN growthRate real;

INSERT INTO schema_version (version_number, comment) VALUES (66, 'Population for regions #82');

-- 2017-09-14 Create history table for UserRoles #328
ALTER TABLE userroles ADD COLUMN sys_period tstzrange;
UPDATE userroles SET sys_period=tstzrange((SELECT users.creationdate FROM users WHERE users.id = userroles.user_id), null);
ALTER TABLE userroles ALTER COLUMN sys_period SET NOT NULL;
CREATE TABLE userroles_history (LIKE userroles);
CREATE TRIGGER versioning_trigger
BEFORE INSERT OR UPDATE OR DELETE ON userroles
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'userroles_history', true);
ALTER TABLE userroles_history OWNER TO sormas_user;

INSERT INTO schema_version (version_number, comment) VALUES (67, 'Create history table for UserRoles #328');

-- 2017-09-18 Symptoms for Yellow fever and Dengue #262
ALTER TABLE symptoms ADD COLUMN backache varchar(255);
ALTER TABLE symptoms ADD COLUMN eyesbleeding varchar(255);
ALTER TABLE symptoms ADD COLUMN jaundice varchar(255);
ALTER TABLE symptoms ADD COLUMN darkurine varchar(255);
ALTER TABLE symptoms ADD COLUMN stomachbleeding varchar(255);
ALTER TABLE symptoms ADD COLUMN rapidbreathing varchar(255);
ALTER TABLE symptoms ADD COLUMN swollenglands varchar(255);

ALTER TABLE cases ADD COLUMN yellowfevervaccination varchar(255);
ALTER TABLE cases ADD COLUMN yellowfevervaccinationinfosource varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (68, 'Additional data for Yellow fever and Dengue #262');

-- 2017-09-21 Disease details field #322
ALTER TABLE cases ADD COLUMN diseasedetails varchar(512);
ALTER TABLE events ADD COLUMN diseasedetails varchar(512);

INSERT INTO schema_version (version_number, comment) VALUES (69, 'Disease details field #322');

-- 2017-09-25 IllLoction removed from symptoms #347
ALTER TABLE symptoms DROP COLUMN illlocation_id;
ALTER TABLE symptoms DROP COLUMN illlocationfrom;
ALTER TABLE symptoms DROP COLUMN illlocationto;
ALTER TABLE symptoms_history DROP COLUMN illlocation_id;
ALTER TABLE symptoms_history DROP COLUMN illlocationfrom;
ALTER TABLE symptoms_history DROP COLUMN illlocationto;

-- 2017-10-26 Add accuracy to lat lon data #371
ALTER TABLE location ADD COLUMN latLonAccuracy real;
ALTER TABLE cases ADD COLUMN reportLatLonAccuracy real;
ALTER TABLE contact ADD COLUMN reportLatLonAccuracy real;
ALTER TABLE events ADD COLUMN reportLatLonAccuracy real;
ALTER TABLE visit ADD COLUMN reportLatLonAccuracy real;
ALTER TABLE task ADD COLUMN closedLatLonAccuracy real;
ALTER TABLE samples ADD COLUMN reportLat double precision;
ALTER TABLE samples ADD COLUMN reportLon double precision;
ALTER TABLE samples ADD COLUMN reportLatLonAccuracy real;

ALTER TABLE location_history ADD COLUMN latLonAccuracy real;
ALTER TABLE cases_history ADD COLUMN reportLatLonAccuracy real;
ALTER TABLE contact_history ADD COLUMN reportLatLonAccuracy real;
ALTER TABLE events_history ADD COLUMN reportLatLonAccuracy real;
ALTER TABLE visit_history ADD COLUMN reportLatLonAccuracy real;
ALTER TABLE task_history ADD COLUMN closedLatLonAccuracy real;
ALTER TABLE samples_history ADD COLUMN reportLat double precision;
ALTER TABLE samples_history ADD COLUMN reportLon double precision;
ALTER TABLE samples_history ADD COLUMN reportLatLonAccuracy real;

INSERT INTO schema_version (version_number, comment) VALUES (71, 'Add accuracy to lat lon data #371');

-- 2017-10-24 Monkeypox disease fields #366
ALTER TABLE symptoms ADD COLUMN cutaneouseruption varchar(255);
ALTER TABLE symptoms ADD COLUMN lesions varchar(255);
ALTER TABLE symptoms ADD COLUMN lesionssamestate varchar(255);
ALTER TABLE symptoms ADD COLUMN lesionssamesize varchar(255);
ALTER TABLE symptoms ADD COLUMN lesionsdeepprofound varchar(255);
ALTER TABLE symptoms ADD COLUMN lesionsface boolean;
ALTER TABLE symptoms ADD COLUMN lesionslegs boolean;
ALTER TABLE symptoms ADD COLUMN lesionssolesfeet boolean;
ALTER TABLE symptoms ADD COLUMN lesionspalmshands boolean;
ALTER TABLE symptoms ADD COLUMN lesionsthorax boolean;
ALTER TABLE symptoms ADD COLUMN lesionsarms boolean;
ALTER TABLE symptoms ADD COLUMN lesionsgenitals boolean;
ALTER TABLE symptoms ADD COLUMN lesionsalloverbody boolean;
ALTER TABLE symptoms ADD COLUMN lesionsresembleimg1 varchar(255);
ALTER TABLE symptoms ADD COLUMN lesionsresembleimg2 varchar(255);
ALTER TABLE symptoms ADD COLUMN lesionsresembleimg3 varchar(255);
ALTER TABLE symptoms ADD COLUMN lesionsresembleimg4 varchar(255);
ALTER TABLE symptoms ADD COLUMN lymphadenopathyinguinal varchar(255);
ALTER TABLE symptoms ADD COLUMN lymphadenopathyaxillary varchar(255);
ALTER TABLE symptoms ADD COLUMN lymphadenopathycervical varchar(255);
ALTER TABLE symptoms ADD COLUMN chillssweats varchar(255);
ALTER TABLE symptoms ADD COLUMN lesionsthatitch varchar(255);
ALTER TABLE symptoms ADD COLUMN bedridden varchar(255);
ALTER TABLE symptoms ADD COLUMN oralulcers varchar(255);
ALTER TABLE symptoms ADD COLUMN patientilllocation varchar(512);
ALTER TABLE cases ADD COLUMN smallpoxVaccinationScar varchar(255);
ALTER TABLE epidata ADD COLUMN dateoflastexposure timestamp;
ALTER TABLE epidata ADD COLUMN placeoflastexposure varchar(512);
ALTER TABLE epidata ADD COLUMN animalcondition varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (72, 'Monkeypox disease fields #366');

-- 2017-10-27 Monkeypox disease fields for history table #366
ALTER TABLE symptoms_history ADD COLUMN cutaneouseruption varchar(255);
ALTER TABLE symptoms_history ADD COLUMN lesions varchar(255);
ALTER TABLE symptoms_history ADD COLUMN lesionssamestate varchar(255);
ALTER TABLE symptoms_history ADD COLUMN lesionssamesize varchar(255);
ALTER TABLE symptoms_history ADD COLUMN lesionsdeepprofound varchar(255);
ALTER TABLE symptoms_history ADD COLUMN lesionsface boolean;
ALTER TABLE symptoms_history ADD COLUMN lesionslegs boolean;
ALTER TABLE symptoms_history ADD COLUMN lesionssolesfeet boolean;
ALTER TABLE symptoms_history ADD COLUMN lesionspalmshands boolean;
ALTER TABLE symptoms_history ADD COLUMN lesionsthorax boolean;
ALTER TABLE symptoms_history ADD COLUMN lesionsarms boolean;
ALTER TABLE symptoms_history ADD COLUMN lesionsgenitals boolean;
ALTER TABLE symptoms_history ADD COLUMN lesionsalloverbody boolean;
ALTER TABLE symptoms_history ADD COLUMN lesionsresembleimg1 varchar(255);
ALTER TABLE symptoms_history ADD COLUMN lesionsresembleimg2 varchar(255);
ALTER TABLE symptoms_history ADD COLUMN lesionsresembleimg3 varchar(255);
ALTER TABLE symptoms_history ADD COLUMN lesionsresembleimg4 varchar(255);
ALTER TABLE symptoms_history ADD COLUMN lymphadenopathyinguinal varchar(255);
ALTER TABLE symptoms_history ADD COLUMN lymphadenopathyaxillary varchar(255);
ALTER TABLE symptoms_history ADD COLUMN lymphadenopathycervical varchar(255);
ALTER TABLE symptoms_history ADD COLUMN chillssweats varchar(255);
ALTER TABLE symptoms_history ADD COLUMN lesionsthatitch varchar(255);
ALTER TABLE symptoms_history ADD COLUMN bedridden varchar(255);
ALTER TABLE symptoms_history ADD COLUMN oralulcers varchar(255);
ALTER TABLE symptoms_history ADD COLUMN patientilllocation varchar(512);
ALTER TABLE cases_history ADD COLUMN smallpoxvaccinationscar varchar(255);
ALTER TABLE epidata_history ADD COLUMN dateoflastexposure timestamp;
ALTER TABLE epidata_history ADD COLUMN placeoflastexposure varchar(512);
ALTER TABLE epidata_history ADD COLUMN animalcondition varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (73, 'Monkeypox disease fields for history table #366');

-- 2017-11-01 Plague disease fields #373
ALTER TABLE symptoms ADD COLUMN painfullymphadenitis varchar(255);
ALTER TABLE symptoms ADD COLUMN buboesgroinarmpitneck varchar(255);
ALTER TABLE symptoms ADD COLUMN blackeningdeathoftissue varchar(255);
ALTER TABLE cases ADD COLUMN plaguetype varchar(255);
ALTER TABLE epidata ADD COLUMN fleabite varchar(255);
ALTER TABLE sampletest ADD COLUMN fourfoldincreaseantibodytiter boolean;

ALTER TABLE symptoms_history ADD COLUMN painfullymphadenitis varchar(255);
ALTER TABLE symptoms_history ADD COLUMN buboesgroinarmpitneck varchar(255);
ALTER TABLE symptoms_history ADD COLUMN blackeningdeathoftissue varchar(255);
ALTER TABLE cases_history ADD COLUMN plaguetype varchar(255);
ALTER TABLE epidata_history ADD COLUMN fleabite varchar(255);
ALTER TABLE sampletest_history ADD COLUMN fourfoldincreaseantibodytiter boolean;

INSERT INTO schema_version (version_number, comment) VALUES (74, 'Plague disease fields #373');

-- 2017-11-02 Additional Monkeypox fields #375
ALTER TABLE cases ADD COLUMN smallpoxvaccinationreceived varchar(255);
ALTER TABLE cases ADD COLUMN smallpoxvaccinationdate timestamp;

ALTER TABLE cases_history ADD COLUMN smallpoxvaccinationreceived varchar(255);
ALTER TABLE cases_history ADD COLUMN smallpoxvaccinationdate timestamp;

INSERT INTO schema_version (version_number, comment) VALUES (75, 'Additional Monkeypox fields #375');

-- 2017-11-22 Population for districts #342
ALTER TABLE district ADD COLUMN population integer;
ALTER TABLE district ADD COLUMN growthRate real;

INSERT INTO schema_version (version_number, comment) VALUES (76, 'Population for districts #342');

-- 2017-11-30 Soft validation for event fields #405
ALTER TABLE events ALTER COLUMN typeofplace DROP NOT NULL;
ALTER TABLE events ALTER COLUMN srcfirstname DROP NOT NULL;
ALTER TABLE events ALTER COLUMN srclastname DROP NOT NULL;
ALTER TABLE events ALTER COLUMN srctelno DROP NOT NULL;

INSERT INTO schema_version (version_number, comment) VALUES (77, 'Soft validation for event fields #405');

-- 2017-12-20 Drop not null constraints from events_history table #405
ALTER TABLE events_history ALTER COLUMN typeofplace DROP NOT NULL;
ALTER TABLE events_history ALTER COLUMN srcfirstname DROP NOT NULL;
ALTER TABLE events_history ALTER COLUMN srclastname DROP NOT NULL;
ALTER TABLE events_history ALTER COLUMN srctelno DROP NOT NULL;

INSERT INTO schema_version (version_number, comment) VALUES (78, 'Drop not null constraints from events_history table #405');

-- 2018-01-03 allow sormas user to dump the complete db and execute update script
ALTER TABLE schema_version OWNER TO sormas_user;

INSERT INTO schema_version (version_number, comment) VALUES (79, 'Give sormas_user access to schema_version table');

-- 2018-01-09 Cause of death fields #439
ALTER TABLE person ADD COLUMN causeofdeath varchar(255);
ALTER TABLE person ADD COLUMN causeofdeathdetails varchar(512);
ALTER TABLE person ADD COLUMN causeofdeathdisease varchar(255);
ALTER TABLE person ADD COLUMN causeofdeathdiseasedetails varchar(512);

INSERT INTO schema_version (version_number, comment) VALUES (80, 'Cause of death fields #439');

-- 2018-01-10 Case outcome #185
ALTER TABLE cases ADD COLUMN outcome varchar(255);
ALTER TABLE cases ADD COLUMN outcomedate timestamp without time zone;

UPDATE cases SET outcome = 'NO_OUTCOME';
UPDATE cases SET changedate = now();

INSERT INTO schema_version (version_number, comment) VALUES (81, 'Case outcome #185');

-- 2018-01-11 Remove unused case date fields #185
ALTER TABLE cases DROP COLUMN suspectdate;
ALTER TABLE cases DROP COLUMN confirmeddate;
ALTER TABLE cases DROP COLUMN negativedate;
ALTER TABLE cases DROP COLUMN nocasedate;
ALTER TABLE cases DROP COLUMN postivedate;
ALTER TABLE cases DROP COLUMN recovereddate;

INSERT INTO schema_version (version_number, comment) VALUES (82, 'Remove unused case date fields #185');

-- 2018-01-11 Cause of death fields #439
ALTER TABLE person DROP COLUMN causeofdeathdiseasedetails;

INSERT INTO schema_version (version_number, comment) VALUES (83, 'Cause of death fields #439');

-- 2018-01-12 New fields for CSM #474
ALTER TABLE hospitalization ADD COLUMN admittedtohealthfacility varchar(255);
ALTER TABLE symptoms ADD COLUMN bulgingfontanelle varchar(255);
ALTER TABLE cases ADD COLUMN vaccination varchar(255);
ALTER TABLE cases ADD COLUMN vaccinationdoses varchar(512);
ALTER TABLE cases ADD COLUMN vaccinationinfosource varchar(255);

UPDATE cases SET vaccination = measlesvaccination where measlesvaccination IS NOT NULL;
UPDATE cases SET vaccination = yellowfevervaccination where yellowfevervaccination IS NOT NULL;
UPDATE cases SET vaccinationdoses = measlesdoses where measlesdoses IS NOT NULL;
UPDATE cases SET vaccinationinfosource = measlesvaccinationinfosource where measlesvaccinationinfosource IS NOT NULL;
UPDATE cases SET vaccinationinfosource = yellowfevervaccinationinfosource where yellowfevervaccinationinfosource IS NOT NULL;

ALTER TABLE cases DROP COLUMN measlesvaccination;
ALTER TABLE cases DROP COLUMN measlesdoses;
ALTER TABLE cases DROP COLUMN measlesvaccinationinfosource;
ALTER TABLE cases DROP COLUMN yellowfevervaccination;
ALTER TABLE cases DROP COLUMN yellowfevervaccinationinfosource;

INSERT INTO schema_version (version_number, comment) VALUES (84, 'New fields for CSM #474');

-- 2018-01-16 Outbreak mode per disease and district #473
CREATE TABLE outbreak(
	id bigint not null,
	uuid varchar(36) not null unique,
	changedate timestamp not null,
	creationdate timestamp not null,
	district_id bigint not null,
	disease varchar(255) not null,
	reportdate timestamp not null,
	reportinguser_id bigint not null,
	primary key(id)
);

ALTER TABLE outbreak OWNER TO sormas_user;
ALTER TABLE outbreak ADD CONSTRAINT fk_outbreak_district_id FOREIGN KEY (district_id) REFERENCES district(id);
ALTER TABLE outbreak ADD CONSTRAINT fk_outbreak_reportinguser_id FOREIGN KEY (reportinguser_id) REFERENCES users(id);

INSERT INTO schema_version (version_number, comment) VALUES (85, 'Outbreak mode per disease and district #473');

-- 2018-01-29 Outbreak history table #473

ALTER TABLE outbreak ADD COLUMN sys_period tstzrange;
UPDATE outbreak SET sys_period=tstzrange(creationdate, null);
ALTER TABLE outbreak ALTER COLUMN sys_period SET NOT NULL;
CREATE TABLE outbreak_history (LIKE outbreak);
CREATE TRIGGER versioning_trigger
BEFORE INSERT OR UPDATE OR DELETE ON outbreak
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'outbreak_history', true);
ALTER TABLE outbreak_history OWNER TO sormas_user;

INSERT INTO schema_version (version_number, comment) VALUES (86, 'Outbreak history table #473');

-- 2018-02-08 Split contact classification into classification and status #454

ALTER TABLE contact ADD COLUMN contactstatus varchar(255);

UPDATE contact SET contactclassification = 'UNCONFIRMED' where contactclassification = 'POSSIBLE';
UPDATE contact SET contactstatus = 'DROPPED' where contactclassification = 'DROPPED';
UPDATE contact SET contactstatus = 'DROPPED' where contactclassification = 'NO_CONTACT';
UPDATE contact SET contactstatus = 'CONVERTED' where contactclassification = 'CONVERTED';
UPDATE contact SET contactstatus = 'ACTIVE' where contactclassification = 'UNCONFIRMED' or contactclassification = 'CONFIRMED';
UPDATE contact SET contactclassification = 'CONFIRMED' where contactclassification = 'CONVERTED' or contactclassification = 'DROPPED';

INSERT INTO schema_version (version_number, comment) VALUES (87, 'Split contact classification into classification and status #454');

-- 2018-02-08 Date of reception #438

ALTER TABLE cases ADD COLUMN receptiondate timestamp without time zone;

INSERT INTO schema_version (version_number, comment) VALUES (88, 'Date of reception #438');

-- 2018-02-08 Date of vaccination for all diseases #486

ALTER TABLE cases RENAME COLUMN smallpoxvaccinationdate TO vaccinationdate;

INSERT INTO schema_version (version_number, comment) VALUES (89, 'Date of vaccination for all diseases #486');

-- 2018-02-09 Monkeypox field changes #401

ALTER TABLE symptoms DROP COLUMN cutaneouseruption;
ALTER TABLE symptoms ADD COLUMN lesionsonsetdate timestamp;
ALTER TABLE symptoms_history DROP COLUMN cutaneouseruption;
ALTER TABLE symptoms_history ADD COLUMN lesionsonsetdate timestamp;

INSERT INTO schema_version (version_number, comment) VALUES (90, 'Monkeypox field changes #401');

-- 2018-02-09 History table updates

ALTER TABLE cases_history DROP COLUMN suspectdate;
ALTER TABLE cases_history DROP COLUMN confirmeddate;
ALTER TABLE cases_history DROP COLUMN negativedate;
ALTER TABLE cases_history DROP COLUMN nocasedate;
ALTER TABLE cases_history DROP COLUMN postivedate;
ALTER TABLE cases_history DROP COLUMN recovereddate;
ALTER TABLE symptoms_history ADD COLUMN bulgingfontanelle varchar(255);
ALTER TABLE cases_history ADD COLUMN vaccination varchar(255);
ALTER TABLE cases_history ADD COLUMN vaccinationdoses varchar(512);
ALTER TABLE cases_history ADD COLUMN vaccinationinfosource varchar(255);
ALTER TABLE cases_history DROP COLUMN measlesvaccination;
ALTER TABLE cases_history DROP COLUMN measlesdoses;
ALTER TABLE cases_history DROP COLUMN measlesvaccinationinfosource;
ALTER TABLE contact_history ADD COLUMN contactstatus varchar(255);
ALTER TABLE cases_history ADD COLUMN receptiondate timestamp without time zone;
ALTER TABLE cases_history RENAME COLUMN smallpoxvaccinationdate TO vaccinationdate;
ALTER TABLE cases_history ADD COLUMN outcome varchar(255);
ALTER TABLE cases_history ADD COLUMN outcomedate timestamp without time zone;
ALTER TABLE person_history ADD COLUMN causeofdeath varchar(255);
ALTER TABLE person_history ADD COLUMN causeofdeathdetails varchar(512);
ALTER TABLE person_history ADD COLUMN causeofdeathdisease varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (91, 'History table updates');

-- 2018-02-13 Test result filter under Samples Directory #482

ALTER TABLE samples ADD COLUMN mainsampletest_id bigint;
ALTER TABLE samples ADD CONSTRAINT fk_samples_mainsampletest_id FOREIGN KEY (mainsampletest_id) REFERENCES sampletest (id);
-- set to latest test, see https://www.periscopedata.com/blog/4-ways-to-join-only-the-first-row-in-sql
UPDATE samples SET mainsampletest_id=(SELECT DISTINCT ON (sample_id) id FROM sampletest WHERE sampletest.sample_id = samples.id ORDER BY sample_id, testdatetime DESC);
ALTER TABLE samples_history ADD COLUMN mainsampletest_id bigint;

INSERT INTO schema_version (version_number, comment) VALUES (92, 'Test result filter under Samples Directory #482');

-- 2018-02-23 Resulting case for contacts #402

ALTER TABLE contact ADD COLUMN resultingcase_id bigint;
ALTER TABLE contact ADD CONSTRAINT fk_contact_resultingcase_id FOREIGN KEY (resultingcase_id) REFERENCES cases (id);
ALTER TABLE contact_history ADD COLUMN resultingcase_id bigint;

INSERT INTO schema_version (version_number, comment) VALUES (93, 'Resulting case for contacts #402');

-- 2018-02-26 Export function for database export #507

CREATE FUNCTION export_database(table_name text, path text, file_name text)
	RETURNS VOID
	LANGUAGE plpgsql
	SECURITY DEFINER
	AS $BODY$
		BEGIN
			EXECUTE '
				COPY (SELECT * FROM
					' || quote_ident(table_name) || '
				) TO
					' || quote_literal(path || file_name) || '
				WITH (
					FORMAT CSV, DELIMITER '';'', HEADER
				);
			';
		END;
	$BODY$
;

CREATE FUNCTION export_database_join(table_name text, join_table_name text, column_name text, join_column_name text, path text, file_name text)
	RETURNS VOID
	LANGUAGE plpgsql
	SECURITY DEFINER
	AS $BODY$
		BEGIN
			EXECUTE '
				COPY (SELECT * FROM
					' || quote_ident(table_name) || '
				INNER JOIN
					' || quote_ident(join_table_name) || '
				ON
					' || column_name || '
				=
					' || join_column_name || '
				) TO
					' || quote_literal(path || file_name) || '
				WITH (
					FORMAT CSV, DELIMITER '';'', HEADER
				);
			';
		END;
	$BODY$
;

INSERT INTO schema_version (version_number, comment) VALUES (94, 'Export function for database export #507');

-- 2018-03-01 Resulting case for event participant #402

ALTER TABLE eventparticipant ADD COLUMN resultingcase_id bigint;
ALTER TABLE eventparticipant ADD CONSTRAINT fk_eventparticipant_resultingcase_id FOREIGN KEY (resultingcase_id) REFERENCES cases (id);

INSERT INTO schema_version (version_number, comment) VALUES (95, 'Resulting case for eventparticipant #402');

-- 2018-03-05 SQL function to easily add new health facilities to the database #519

CREATE OR REPLACE FUNCTION add_health_facility(_name character varying(512), _islaboratory boolean, _regionname character varying(512), _districtname character varying(512), _communityname character varying(512), _city character varying(512), _latitude double precision, _longitude double precision)
RETURNS bigint AS $resultid$
DECLARE
	_type character varying(512);
	_id bigint;
	_uuid character varying(36);
	_region_id bigint;
	_district_id bigint;
	_community_id bigint;
BEGIN
	IF (_regionname IS NULL) THEN
		RAISE EXCEPTION 'you need to pass a region name';
	END IF;
	IF (NOT(_islaboratory) AND _districtname IS NULL) THEN
		RAISE EXCEPTION 'you need to pass a district name';
	END IF;

	IF (_islaboratory) THEN
		_type = 'LABORATORY';
	END IF;

	SELECT region.id FROM region WHERE region.name = _regionname INTO _region_id;
	SELECT district.id FROM district WHERE district.name = _districtname AND district.region_id = _region_id INTO _district_id;
	SELECT community.id FROM community WHERE community.name = _communityname AND community.district_id = _district_id INTO _community_id;

	IF (_regionname IS NOT NULL AND _region_id IS NULL) THEN
		RAISE EXCEPTION 'region not found %', _regionname;
	END IF;
	IF (_districtname IS NOT NULL AND _district_id IS NULL) THEN
		RAISE EXCEPTION 'district not found %', _districtname;
	END IF;
	IF (_communityname IS NOT NULL AND _community_id IS NULL) THEN
		RAISE EXCEPTION 'community not found %', _communityname;
	END IF;

	_id = nextval('entity_seq');
	-- this is not the same format as we are using in code (which is base32 with 4 seperators)
	_uuid = upper(substring(CAST(CAST(md5(CAST(random() AS text) || CAST(clock_timestamp() AS text)) AS uuid) AS text), 3, 29));

	IF ((SELECT facility.id FROM facility WHERE facility.name = _name AND facility.region_id = _region_id AND facility.district_id = _district_id) IS NOT NULL) THEN
		RAISE EXCEPTION 'facility % allready exists in district', _name;
	END IF;

	INSERT INTO facility(
            id, changedate, creationdate, name, publicownership, type, uuid,
            region_id, district_id, community_id, city, latitude, longitude)
	VALUES (_id, now(), now(), _name, FALSE, _type, _uuid,
            _region_id, _district_id, _community_id, _city, _latitude, _longitude);

        RETURN _id;
END;
$resultid$  LANGUAGE plpgsql;

INSERT INTO schema_version (version_number, comment) VALUES (96, 'SQL function to easily add new health facilities to the database #519');

-- 2018-03-05 Add upgrade column to schema_version for backend upgrade logic #402
ALTER TABLE schema_version ADD COLUMN upgradeNeeded boolean NOT NULL DEFAULT false;
UPDATE schema_version SET upgradeNeeded=true WHERE version_number=95;
-- fixed 2019-06-25 #1126
GRANT ALL ON TABLE schema_version TO sormas_user;

INSERT INTO schema_version (version_number, comment) VALUES (97, 'Add upgrade column to schema_version for backend upgrade logic #402');

-- 2018-03-06 Restrict outbreak mode to CSM for now
DELETE FROM outbreak WHERE NOT (disease = 'CSM');

INSERT INTO schema_version (version_number, comment) VALUES (98, 'Restrict outbreak mode to CSM for now #489');

-- 2018-03-06 Change export functions #507
DROP FUNCTION export_database(text, text, text);
DROP FUNCTION export_database_join(text, text, text, text, text, text);

CREATE FUNCTION export_database(table_name text, file_path text)
	RETURNS VOID
	LANGUAGE plpgsql
	SECURITY DEFINER
	AS $BODY$
		BEGIN
			EXECUTE '
				COPY (SELECT * FROM
					' || quote_ident(table_name) || '
				) TO
					' || quote_literal(file_path) || '
				WITH (
					FORMAT CSV, DELIMITER '';'', HEADER
				);
			';
		END;
	$BODY$
;

CREATE FUNCTION export_database_join(table_name text, join_table_name text, column_name text, join_column_name text, file_path text)
	RETURNS VOID
	LANGUAGE plpgsql
	SECURITY DEFINER
	AS $BODY$
		BEGIN
			EXECUTE '
				COPY (SELECT * FROM
					' || quote_ident(table_name) || '
				INNER JOIN
					' || quote_ident(join_table_name) || '
				ON
					' || column_name || '
				=
					' || join_column_name || '
				) TO
					' || quote_literal(file_path) || '
				WITH (
					FORMAT CSV, DELIMITER '';'', HEADER
				);
			';
		END;
	$BODY$
;

INSERT INTO schema_version (version_number, comment) VALUES (99, 'Change export functions #507');

-- 2018-03-09 Create history table for event participant #509
ALTER TABLE eventparticipant ADD COLUMN sys_period tstzrange;
UPDATE eventparticipant SET sys_period=tstzrange(creationdate, null);
ALTER TABLE eventparticipant ALTER COLUMN sys_period SET NOT NULL;
CREATE TABLE eventparticipant_history (LIKE eventparticipant);
CREATE TRIGGER versioning_trigger
BEFORE INSERT OR UPDATE OR DELETE ON eventparticipant
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'eventparticipant_history', true);
ALTER TABLE eventparticipant_history OWNER TO sormas_user;

INSERT INTO schema_version (version_number, comment) VALUES (100, 'Create history table for event participant #509');

-- 2018-04-12 Remove cause of death disease from persons that died due to another cause
UPDATE person SET causeofdeathdisease = null WHERE causeofdeath != 'EPIDEMIC_DISEASE';

INSERT INTO schema_version (version_number, comment) VALUES (101, 'Remove cause of death disease from persons that died due to another cause');

-- 2018-05-07 Case age field #585
ALTER TABLE cases ADD COLUMN caseage integer;
ALTER TABLE cases_history ADD COLUMN caseage integer;

UPDATE cases SET caseage = p.approximateage FROM person p WHERE cases.person_id = p.id AND p.approximateage IS NOT NULL AND p.approximateagetype = 0;
UPDATE cases SET caseage = 0 FROM person p WHERE cases.person_id = p.id AND p.approximateage IS NOT NULL AND p.approximateagetype = 1;

INSERT INTO schema_version (version_number, comment) VALUES (102, 'Case age field #585');

-- 2018-05-12 epi week functions #541

-- week starts monday
-- 1st of january is always in week 1
CREATE OR REPLACE FUNCTION epi_week (indate timestamp)
RETURNS integer AS
$result$
DECLARE year integer;
	doy integer;
	doy_end integer;
	isodow_start_next integer;
	isodow_start integer;
	epi_week integer;
BEGIN
   year := date_part('year', indate);
   -- days until end of year
   -- DOY: The day of the year (1 - 365/366)
   doy = date_part('DOY', indate);
   doy_end := date_part('DOY', date (year || '-12-31'));
   -- week day of first day in next year
   -- isodow: The day of the week as Monday (1) to Sunday (7)
   isodow_start_next := date_part('isodow', date ((year+1) || '-01-01'));
   -- check end of date year
   -- DOY 31.12 - DOY < DOY(01.01.Y+1)
   if (doy_end - doy < isodow_start_next-1) THEN
	-- falls into next epi year
	epi_week := 1;
   ELSE
	-- 2018: 01.01 is monday -> ceil(doy/7) = epi-week
	-- 2017: 01.01 is sunday -> ceil((doy+6)/7) = epi-week
	-- 2016: 01.01 is friday -> ceil((doy+4)/7) = epi-week
	isodow_start := date_part('isodow', date (year || '-01-01'));
	epi_week := ceil((doy + isodow_start - 1) / 7.0);
   END if;

   RETURN epi_week;
END;
$result$
LANGUAGE plpgsql;

-- see epi_week
CREATE OR REPLACE FUNCTION epi_year (indate timestamp)
RETURNS integer AS
$result$
DECLARE year integer;
	doy integer;
	doy_end integer;
	isodow_start_next integer;
	isodow_start integer;
	epi_year integer;
BEGIN
   year := date_part('year', indate);
   doy = date_part('DOY', indate);
   doy_end := date_part('DOY', date (year || '-12-31'));
   isodow_start_next := date_part('isodow', date ((year+1) || '-01-01'));
   if (doy_end - doy < isodow_start_next-1) THEN
	-- next year
	epi_year := year+1;
   ELSE
	epi_year := year;
   END if;

   RETURN epi_year;
END;
$result$
LANGUAGE plpgsql;

-- e.g. SELECT epi_week('2015-12-27'), epi_year('2015-12-27'); -- 52-2015
-- e.g. SELECT epi_week('2015-12-28'), epi_year('2015-12-28'); -- 01-2016

INSERT INTO schema_version (version_number, comment) VALUES (103, 'Epi week functions #541');

-- 2018-05-28 Creating new event in app not working #614

ALTER TABLE events ALTER COLUMN typeofplace DROP NOT NULL;
ALTER TABLE events ALTER COLUMN srcfirstname DROP NOT NULL;
ALTER TABLE events ALTER COLUMN srclastname DROP NOT NULL;
ALTER TABLE events ALTER COLUMN srctelno DROP NOT NULL;
ALTER TABLE events_history ALTER COLUMN typeofplace DROP NOT NULL;
ALTER TABLE events_history ALTER COLUMN srcfirstname DROP NOT NULL;
ALTER TABLE events_history ALTER COLUMN srclastname DROP NOT NULL;
ALTER TABLE events_history ALTER COLUMN srctelno DROP NOT NULL;

INSERT INTO schema_version (version_number, comment) VALUES (104, 'Creating new event in app not working #614');

-- 2018-05-31 Case data changes for automatic case classification #628

ALTER TABLE cases ADD COLUMN denguefevertype character varying(255);
ALTER TABLE cases ADD COLUMN classificationcomment character varying(512);
ALTER TABLE cases ADD COLUMN classificationdate timestamp without time zone;
ALTER TABLE cases ADD COLUMN classificationuser_id bigint;
ALTER TABLE cases ADD CONSTRAINT fk_cases_classificationuser_id FOREIGN KEY (classificationuser_id) REFERENCES public.users (id);

UPDATE cases SET disease = 'NEW_INFLUENCA' where disease = 'AVIAN_INFLUENCA';
UPDATE cases_history SET disease = 'NEW_INFLUENCA' where disease = 'AVIAN_INFLUENCA';
UPDATE events SET disease = 'NEW_INFLUENCA' where disease = 'AVIAN_INFLUENCA';
UPDATE events_history SET disease = 'NEW_INFLUENCA' where disease = 'AVIAN_INFLUENCA';
UPDATE outbreak SET disease = 'NEW_INFLUENCA' where disease = 'AVIAN_INFLUENCA';
UPDATE outbreak_history SET disease = 'NEW_INFLUENCA' where disease = 'AVIAN_INFLUENCA';
UPDATE person SET causeofdeathdisease = 'NEW_INFLUENCA' where causeofdeathdisease = 'AVIAN_INFLUENCA';
UPDATE person_history SET causeofdeathdisease = 'NEW_INFLUENCA' where causeofdeathdisease = 'AVIAN_INFLUENCA';
UPDATE visit SET disease = 'NEW_INFLUENCA' where disease = 'AVIAN_INFLUENCA';
UPDATE visit_history SET disease = 'NEW_INFLUENCA' where disease = 'AVIAN_INFLUENCA';
UPDATE weeklyreportentry SET disease = 'NEW_INFLUENCA' where disease = 'AVIAN_INFLUENCA';

INSERT INTO schema_version (version_number, comment) VALUES (105, 'Case data changes for automatic case classification #628');

-- 2018-06-01 Add description field for other health facility to previous hospitalizations and person occupations #549

ALTER TABLE previoushospitalization ADD COLUMN healthfacilitydetails varchar(512);
ALTER TABLE previoushospitalization_history ADD COLUMN healthfacilitydetails varchar(512);
ALTER TABLE person ADD COLUMN occupationfacilitydetails varchar(512);
ALTER TABLE person_history ADD COLUMN occupationfacilitydetails varchar(512);
ALTER TABLE person ADD COLUMN occupationregion_id bigint;
ALTER TABLE person ADD COLUMN occupationdistrict_id bigint;
ALTER TABLE person ADD COLUMN occupationcommunity_id bigint;
ALTER TABLE person_history ADD COLUMN occupationregion_id bigint;
ALTER TABLE person_history ADD COLUMN occupationdistrict_id bigint;
ALTER TABLE person_history ADD COLUMN occupationcommunity_id bigint;

ALTER TABLE person ADD CONSTRAINT fk_person_occupationregion_id FOREIGN KEY (occupationregion_id) REFERENCES region (id);
ALTER TABLE person ADD CONSTRAINT fk_person_occupationdistrict_id FOREIGN KEY (occupationdistrict_id) REFERENCES district (id);
ALTER TABLE person ADD CONSTRAINT fk_person_occupationcommunity_id FOREIGN KEY (occupationcommunity_id) REFERENCES community (id);

UPDATE person SET occupationregion_id = (SELECT region_id FROM facility WHERE facility.id = person.occupationfacility_id) WHERE occupationfacility_id IS NOT NULL;
UPDATE person SET occupationdistrict_id = (SELECT district_id FROM facility WHERE facility.id = person.occupationfacility_id) WHERE occupationfacility_id IS NOT NULL;
UPDATE person SET occupationcommunity_id = (SELECT community_id FROM facility WHERE facility.id = person.occupationfacility_id) WHERE occupationfacility_id IS NOT NULL;

-- 2018-06-01 Case symptoms changes for automatic case classification #631

ALTER TABLE symptoms ADD COLUMN meningealsigns varchar(255);
ALTER TABLE symptoms_history ADD COLUMN meningealsigns varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (107, 'Case symptoms changes for automatic case classification #631');

-- 2018-06-04 Add "Other laboratory" option for sample and sample test #440

ALTER TABLE samples ADD COLUMN labdetails varchar(512);
ALTER TABLE samples_history ADD COLUMN labdetails varchar(512);
ALTER TABLE sampletest ADD COLUMN labdetails varchar(512);
ALTER TABLE sampletest_history ADD COLUMN labdetails varchar(512);

INSERT INTO schema_version (version_number, comment) VALUES (108, 'Add "Other laboratory" option for sample and sample test #440');

-- 2018-06-05 Sample and sample test data model changes #627

UPDATE sampletest SET testtype = 'IGM_SERUM_ANTIBODY' WHERE testtype = 'SERUM_ANTIBODY_TITER';
UPDATE sampletest SET testtype = 'IGM_SERUM_ANTIBODY' WHERE testtype = 'ELISA';
UPDATE sampletest SET testtype = 'PCR_RT_PCR' WHERE testtype = 'PCR' OR testtype = 'RT_PCR';
UPDATE sampletest_history SET testtype = 'IGM_SERUM_ANTIBODY' WHERE testtype = 'SERUM_ANTIBODY_TITER';
UPDATE sampletest_history SET testtype = 'IGM_SERUM_ANTIBODY' WHERE testtype = 'ELISA';
UPDATE sampletest_history SET testtype = 'PCR_RT_PCR' WHERE testtype = 'PCR' OR testtype = 'RT_PCR';
UPDATE samples SET suggestedtypeoftest = 'IGM_SERUM_ANTIBODY' WHERE suggestedtypeoftest = 'SERUM_ANTIBODY_TITER';
UPDATE samples SET suggestedtypeoftest = 'IGM_SERUM_ANTIBODY' WHERE suggestedtypeoftest = 'ELISA';
UPDATE samples SET suggestedtypeoftest = 'PCR_RT_PCR' WHERE suggestedtypeoftest = 'PCR' OR suggestedtypeoftest = 'RT_PCR';
UPDATE samples_history SET suggestedtypeoftest = 'IGM_SERUM_ANTIBODY' WHERE suggestedtypeoftest = 'SERUM_ANTIBODY_TITER';
UPDATE samples_history SET suggestedtypeoftest = 'IGM_SERUM_ANTIBODY' WHERE suggestedtypeoftest = 'ELISA';
UPDATE samples_history SET suggestedtypeoftest = 'PCR_RT_PCR' WHERE suggestedtypeoftest = 'PCR' OR suggestedtypeoftest = 'RT_PCR';

INSERT INTO schema_version (version_number, comment) VALUES (109, 'Sample and sample test data model changes #627');

-- 2018-06-04 Case epi data changes for automatic case classification #632

ALTER TABLE epidata ADD COLUMN directcontactconfirmedcase varchar(255);
ALTER TABLE epidata ADD COLUMN directcontactprobablecase varchar(255);
ALTER TABLE epidata ADD COLUMN closecontactprobablecase varchar(255);
ALTER TABLE epidata ADD COLUMN areaconfirmedcases varchar(255);
ALTER TABLE epidata ADD COLUMN processingconfirmedcasefluidunsafe varchar(255);
ALTER TABLE epidata ADD COLUMN percutaneouscaseblood varchar(255);
ALTER TABLE epidata ADD COLUMN directcontactdeadunsafe varchar(255);
ALTER TABLE epidata ADD COLUMN processingsuspectedcasesampleunsafe varchar(255);
ALTER TABLE epidata ADD COLUMN areainfectedanimals varchar(255);
ALTER TABLE epidata RENAME COLUMN poultrysick TO sickdeadanimals;
ALTER TABLE epidata RENAME COLUMN poultrysickdetails TO sickdeadanimalsdetails;
ALTER TABLE epidata RENAME COLUMN poultrydate TO sickdeadanimalsdate;
ALTER TABLE epidata RENAME COLUMN poultrylocation TO sickdeadanimalslocation;
ALTER TABLE epidata ADD COLUMN eatingrawanimalsininfectedarea varchar(255);
ALTER TABLE epidata RENAME COLUMN poultryeat TO eatingrawanimals;
ALTER TABLE epidata ADD COLUMN eatingrawanimalsdetails varchar(512);
ALTER TABLE epidata DROP COLUMN poultry;
ALTER TABLE epidata DROP COLUMN poultrydetails;
ALTER TABLE epidata DROP COLUMN wildbirds;
ALTER TABLE epidata DROP COLUMN wildbirdsdetails;
ALTER TABLE epidata DROP COLUMN wildbirdsdate;
ALTER TABLE epidata DROP COLUMN wildbirdslocation;

ALTER TABLE epidata_history ADD COLUMN directcontactconfirmedcase varchar(255);
ALTER TABLE epidata_history ADD COLUMN directcontactprobablecase varchar(255);
ALTER TABLE epidata_history ADD COLUMN closecontactprobablecase varchar(255);
ALTER TABLE epidata_history ADD COLUMN areaconfirmedcases varchar(255);
ALTER TABLE epidata_history ADD COLUMN processingconfirmedcasefluidunsafe varchar(255);
ALTER TABLE epidata_history ADD COLUMN percutaneouscaseblood varchar(255);
ALTER TABLE epidata_history ADD COLUMN directcontactdeadunsafe varchar(255);
ALTER TABLE epidata_history ADD COLUMN processingsuspectedcasesampleunsafe varchar(255);
ALTER TABLE epidata_history ADD COLUMN areainfectedanimals varchar(255);
ALTER TABLE epidata_history RENAME COLUMN poultrysick TO sickdeadanimals;
ALTER TABLE epidata_history RENAME COLUMN poultrysickdetails TO sickdeadanimalsdetails;
ALTER TABLE epidata_history RENAME COLUMN poultrydate TO sickdeadanimalsdate;
ALTER TABLE epidata_history RENAME COLUMN poultrylocation TO sickdeadanimalslocation;
ALTER TABLE epidata_history ADD COLUMN eatingrawanimalsininfectedarea varchar(255);
ALTER TABLE epidata_history RENAME COLUMN poultryeat TO eatingrawanimals;
ALTER TABLE epidata_history ADD COLUMN eatingrawanimalsdetails varchar(512);
ALTER TABLE epidata_history DROP COLUMN poultry;
ALTER TABLE epidata_history DROP COLUMN poultrydetails;
ALTER TABLE epidata_history DROP COLUMN wildbirds;
ALTER TABLE epidata_history DROP COLUMN wildbirdsdetails;
ALTER TABLE epidata_history DROP COLUMN wildbirdsdate;
ALTER TABLE epidata_history DROP COLUMN wildbirdslocation;

INSERT INTO schema_version (version_number, comment) VALUES (110, 'Case epi data changes for automatic case classification #632');

-- 2018-06-04 Add "contact with source case" to epi data #629

ALTER TABLE contact ADD COLUMN resultingcaseuser_id bigint;
ALTER TABLE contact_history ADD COLUMN resultingcaseuser_id bigint;
ALTER TABLE contact ADD CONSTRAINT fk_contact_resultingcaseuser_id FOREIGN KEY (resultingcaseuser_id) REFERENCES public.users (id);

INSERT INTO schema_version (version_number, comment) VALUES (111, 'Add "contact with source case" to epi data #629');

-- 2018-06-05 Rename Rumor Manager user role to "Event Officer" #633

UPDATE userroles SET userrole = 'EVENT_OFFICER' WHERE userrole = 'RUMOR_MANAGER';
UPDATE userroles_history SET userrole = 'EVENT_OFFICER' WHERE userrole = 'RUMOR_MANAGER';

INSERT INTO schema_version (version_number, comment) VALUES (112, 'Rename Rumor Manager user role to "Event Officer" #633');

-- 2018-08-17 Automatic case classification for existing SORMAS diseases #61

ALTER TABLE cases ADD COLUMN systemcaseclassification character varying(255) DEFAULT 'NOT_CLASSIFIED' NOT NULL;
ALTER TABLE cases_history ADD COLUMN systemcaseclassification character varying(255);

INSERT INTO schema_version (version_number, comment) VALUES (113, 'System case classification column #61');

-- 2018-10-23 Archiving for cases and events #843

ALTER TABLE cases ADD COLUMN archived boolean NOT NULL DEFAULT false;
ALTER TABLE events ADD COLUMN archived boolean NOT NULL DEFAULT false;
ALTER TABLE cases_history ADD COLUMN archived boolean;
ALTER TABLE events_history ADD COLUMN archived boolean;

INSERT INTO schema_version (version_number, comment) VALUES (114, 'Archiving for cases and events #843');

-- 2018-11-16 Change dates with year 18 to 2018 #792

UPDATE cases SET receptiondate = receptiondate + interval '2000 years' WHERE EXTRACT(year FROM receptiondate) = 18;
UPDATE cases SET investigateddate = investigateddate + interval '2000 years' WHERE EXTRACT(year FROM investigateddate) = 18;
UPDATE symptoms SET onsetdate = onsetdate + interval '2000 years' WHERE EXTRACT(year FROM onsetdate) = 18;

INSERT INTO schema_version (version_number, comment) VALUES (115, 'Change dates with year 18 to 2018 #792');

-- 2018-12-03 Community Informant user role #872

UPDATE userroles SET userrole = REPLACE(userrole, 'INFORMANT', 'HOSPITAL_INFORMANT');
UPDATE userroles_history SET userrole = REPLACE(userrole, 'INFORMANT', 'HOSPITAL_INFORMANT');
ALTER TABLE users ADD COLUMN community_id bigint;
ALTER TABLE users ADD CONSTRAINT fk_users_community_id FOREIGN KEY (community_id) REFERENCES community (id);

INSERT INTO schema_version (version_number, comment) VALUES (116, 'Community informant user role #872');

-- 2018-12-03 Rename useroles to user_userroles #830

DROP TRIGGER versioning_trigger ON userroles;
ALTER TABLE userroles RENAME TO users_userroles;
ALTER TABLE userroles_history RENAME TO users_userroles_history;
CREATE TRIGGER versioning_trigger
BEFORE INSERT OR UPDATE OR DELETE ON users_userroles
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'users_userroles_history', true);

INSERT INTO schema_version (version_number, comment) VALUES (117, 'Rename useroles to users_userroles #830');

-- 2018-12-03 User role configuration #830

CREATE TABLE userrolesconfig (
	id bigint not null,
	uuid varchar(36) not null unique,
	changedate timestamp not null,
	creationdate timestamp not null,
	userrole varchar(255) not null unique,
	sys_period tstzrange NOT NULL,
	primary key(id)
);
ALTER TABLE userrolesconfig OWNER TO sormas_user;

CREATE TABLE userrolesconfig_history (LIKE userrolesconfig);
CREATE TRIGGER versioning_trigger BEFORE INSERT OR UPDATE OR DELETE ON userrolesconfig
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'userrolesconfig_history', true);
ALTER TABLE userrolesconfig_history OWNER TO sormas_user;

CREATE TABLE userroles_userrights (
    userrole character varying(255) NOT NULL,
    userright character varying(255) NOT NULL,
	sys_period tstzrange NOT NULL
);
ALTER TABLE userroles_userrights OWNER TO sormas_user;

ALTER TABLE ONLY userroles_userrights
    ADD CONSTRAINT unq_userroles_userrights_0 UNIQUE (userrole, userright);
ALTER TABLE ONLY userroles_userrights
    ADD CONSTRAINT fk_userroles_userrights_user_id FOREIGN KEY (userrole) REFERENCES userrolesconfig(userrole);

CREATE TABLE userroles_userrights_history (LIKE userroles_userrights);
CREATE TRIGGER versioning_trigger BEFORE INSERT OR UPDATE OR DELETE ON userroles_userrights
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'userroles_userrights_history', true);
ALTER TABLE userroles_userrights_history OWNER TO sormas_user;

INSERT INTO schema_version (version_number, comment) VALUES (118, 'User role configuration #830');

ALTER TABLE userroles_userrights ADD COLUMN userrole_id bigint NOT NULL;
ALTER TABLE userroles_userrights DROP CONSTRAINT fk_userroles_userrights_user_id;
ALTER TABLE userroles_userrights DROP CONSTRAINT unq_userroles_userrights_0;
ALTER TABLE userroles_userrights DROP COLUMN userrole;
ALTER TABLE ONLY userroles_userrights
    ADD CONSTRAINT unq_userroles_userrights_0 UNIQUE (userrole_id, userright);
ALTER TABLE ONLY userroles_userrights
    ADD CONSTRAINT fk_userroles_userrights_userrole_id FOREIGN KEY (userrole_id) REFERENCES userrolesconfig(id);

DROP TABLE userroles_userrights_history;
CREATE TABLE userroles_userrights_history (LIKE userroles_userrights);
ALTER TABLE userroles_userrights_history OWNER TO sormas_user;

INSERT INTO schema_version (version_number, comment) VALUES (119, 'Fix for user role configuration #830');

-- 2018-12-04 Restructuring weekly reports #610

ALTER TABLE weeklyreport ALTER COLUMN healthfacility_id DROP NOT NULL;
ALTER TABLE weeklyreport RENAME informant_id TO reportinguser_id;
ALTER TABLE weeklyreport ADD COLUMN district_id bigint;
ALTER TABLE weeklyreport ADD CONSTRAINT fk_weeklyreport_district_id FOREIGN KEY (district_id) REFERENCES district (id);
ALTER TABLE weeklyreport ADD COLUMN community_id bigint;
ALTER TABLE weeklyreport ADD CONSTRAINT fk_weeklyreport_commuinty_id FOREIGN KEY (community_id) REFERENCES community (id);
ALTER TABLE weeklyreport ADD COLUMN assignedofficer_id bigint;
ALTER TABLE weeklyreport ADD CONSTRAINT fk_weeklyreport_assignedofficer_id FOREIGN KEY (assignedofficer_id) REFERENCES users (id);

ALTER TABLE users_history ADD COLUMN community_id bigint;
ALTER TABLE users_history ADD CONSTRAINT fk_users_community_id FOREIGN KEY (community_id) REFERENCES community (id);

INSERT INTO schema_version (version_number, comment) VALUES (120, 'Restructuring weekly reports #610');

-- 2018-12-10 Outbreak start & end #889

ALTER TABLE outbreak ADD COLUMN startdate timestamp;
UPDATE outbreak SET startdate=creationdate;
ALTER TABLE outbreak ALTER COLUMN startdate SET NOT NULL;
ALTER TABLE outbreak ADD COLUMN enddate timestamp;
ALTER TABLE outbreak_history ADD COLUMN startdate timestamp;
ALTER TABLE outbreak_history ADD COLUMN enddate timestamp;

INSERT INTO schema_version (version_number, comment) VALUES (121, 'Outbreak start & end #889');

-- 2018-12-13 Virus isolation -> Isolation #838

UPDATE samples SET suggestedtypeoftest='ISOLATION' WHERE suggestedtypeoftest='VIRUS_ISOLATION';
UPDATE samples_history SET suggestedtypeoftest='ISOLATION' WHERE suggestedtypeoftest='VIRUS_ISOLATION';
UPDATE sampletest SET testtype='ISOLATION' WHERE testtype='VIRUS_ISOLATION';
UPDATE sampletest_history SET testtype='ISOLATION' WHERE testtype='VIRUS_ISOLATION';

INSERT INTO schema_version (version_number, comment) VALUES (122, 'Virus isolation -> Isolation #838');

-- 2018-12-19 History table and change date for embedded weekly report lists #610

ALTER TABLE weeklyreport ADD COLUMN changedateofembeddedlists timestamp without time zone;

ALTER TABLE weeklyreport ADD COLUMN sys_period tstzrange;
UPDATE weeklyreport SET sys_period=tstzrange(creationdate, null);
ALTER TABLE weeklyreport ALTER COLUMN sys_period SET NOT NULL;
CREATE TABLE weeklyreport_history (LIKE weeklyreport);
CREATE TRIGGER versioning_trigger
BEFORE INSERT OR UPDATE OR DELETE ON weeklyreport
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'weeklyreport_history', true);
ALTER TABLE weeklyreport_history OWNER TO sormas_user;

ALTER TABLE weeklyreportentry ADD COLUMN sys_period tstzrange;
UPDATE weeklyreportentry SET sys_period=tstzrange(creationdate, null);
ALTER TABLE weeklyreportentry ALTER COLUMN sys_period SET NOT NULL;
CREATE TABLE weeklyreportentry_history (LIKE weeklyreportentry);
CREATE TRIGGER versioning_trigger
BEFORE INSERT OR UPDATE OR DELETE ON weeklyreportentry
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'weeklyreportentry_history', true);
ALTER TABLE weeklyreportentry_history OWNER TO sormas_user;

INSERT INTO schema_version (version_number, comment) VALUES (123, 'History table and change date for embedded weekly report lists #610');

-- 2019-01-29 Therapy, treatment and prescription tables #936

CREATE TABLE therapy(
	id bigint not null,
	uuid varchar(36) not null unique,
	changedate timestamp not null,
	creationdate timestamp not null,
	sys_period tstzrange not null,
	primary key(id)
);
ALTER TABLE therapy OWNER TO sormas_user;

CREATE TABLE therapy_history (LIKE therapy);
CREATE TRIGGER versioning_trigger
BEFORE INSERT OR UPDATE OR DELETE ON therapy
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'therapy_history', true);
ALTER TABLE therapy_history OWNER TO sormas_user;

CREATE TABLE prescription(
	id bigint not null,
	uuid varchar(36) not null unique,
	changedate timestamp not null,
	creationdate timestamp not null,
	therapy_id bigint not null,
	prescriptiondate timestamp not null,
	prescriptionstart timestamp,
	prescriptionend timestamp,
	prescribingclinician varchar(512),
	prescriptiontype varchar(255) not null,
	prescriptiondetails varchar(512),
	frequency varchar(512),
	dose varchar(512),
	route varchar(255),
	routedetails varchar(512),
	additionalnotes varchar(512),
	sys_period tstzrange not null,
	primary key(id)
);
ALTER TABLE prescription OWNER TO sormas_user;
ALTER TABLE prescription ADD CONSTRAINT fk_prescription_therapy_id FOREIGN KEY (therapy_id) REFERENCES therapy (id);

CREATE TABLE prescription_history (LIKE prescription);
CREATE TRIGGER versioning_trigger
BEFORE INSERT OR UPDATE OR DELETE ON prescription
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'prescription_history', true);
ALTER TABLE prescription_history OWNER TO sormas_user;

CREATE TABLE treatment(
	id bigint not null,
	uuid varchar(36) not null unique,
	changedate timestamp not null,
	creationdate timestamp not null,
	therapy_id bigint not null,
	treatmentdatetime timestamp not null,
	executingclinician varchar(512),
	treatmenttype varchar(255) not null,
	treatmentdetails varchar(512),
	dose varchar(512),
	route varchar(255),
	routedetails varchar(512),
	additionalnotes varchar(512),
	sys_period tstzrange not null,
	primary key(id)
);
ALTER TABLE treatment OWNER TO sormas_user;
ALTER TABLE treatment ADD CONSTRAINT fk_treatment_therapy_id FOREIGN KEY (therapy_id) REFERENCES therapy (id);

CREATE TABLE treatment_history (LIKE treatment);
CREATE TRIGGER versioning_trigger
BEFORE INSERT OR UPDATE OR DELETE ON treatment
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'treatment_history', true);
ALTER TABLE treatment_history OWNER TO sormas_user;

ALTER TABLE cases ADD COLUMN therapy_id bigint;
ALTER TABLE cases_history ADD COLUMN therapy_id bigint;
ALTER TABLE cases ADD CONSTRAINT fk_cases_therapy_id FOREIGN KEY (therapy_id) REFERENCES therapy (id);

INSERT INTO schema_version (version_number, comment) VALUES (125, 'Therapy, treatment and prescription tables #936');

-- 2019-01-30 Type of drug and prescription link #936

ALTER TABLE prescription ADD COLUMN typeofdrug varchar(255);
ALTER TABLE prescription_history ADD COLUMN typeofdrug varchar(255);
ALTER TABLE treatment ADD COLUMN typeofdrug varchar(255);
ALTER TABLE treatment_history ADD COLUMN typeofdrug varchar(255);
ALTER TABLE treatment ADD COLUMN prescription_id bigint;
ALTER TABLE treatment_history ADD COLUMN prescription_id bigint;

ALTER TABLE treatment ADD CONSTRAINT fk_treatment_prescription_id FOREIGN KEY (prescription_id) REFERENCES prescription (id);

INSERT INTO schema_version (version_number, comment) VALUES (126, 'Type of drug and prescription link #936');

-- 2019-02-11 Clinical course and visits #938

CREATE TABLE clinicalcourse(
	id bigint not null,
	uuid varchar(36) not null unique,
	changedate timestamp not null,
	creationdate timestamp not null,
	sys_period tstzrange not null,
	primary key(id)
);
ALTER TABLE clinicalcourse OWNER TO sormas_user;

CREATE TABLE clinicalcourse_history (LIKE clinicalcourse);
CREATE TRIGGER versioning_trigger
BEFORE INSERT OR UPDATE OR DELETE ON clinicalcourse
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'clinicalcourse_history', true);
ALTER TABLE clinicalcourse_history OWNER TO sormas_user;

CREATE TABLE clinicalvisit(
	id bigint not null,
	uuid varchar(36) not null unique,
	changedate timestamp not null,
	creationdate timestamp not null,
	clinicalcourse_id bigint not null,
	symptoms_id bigint not null,
	person_id bigint not null,
	disease varchar(255),
	visitdatetime timestamp,
	visitremarks varchar(512),
	visitingperson varchar(512),
	sys_period tstzrange not null,
	primary key(id)
);
ALTER TABLE clinicalvisit OWNER TO sormas_user;
ALTER TABLE clinicalvisit ADD CONSTRAINT fk_clinicalvisit_clinicalcourse_id FOREIGN KEY (clinicalcourse_id) REFERENCES clinicalcourse (id);
ALTER TABLE clinicalvisit ADD CONSTRAINT fk_clinicalvisit_symptoms_id FOREIGN KEY (symptoms_id) REFERENCES symptoms (id) ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE clinicalvisit ADD CONSTRAINT fk_clinicalvisit_person_id FOREIGN KEY (person_id) REFERENCES person (id);

CREATE TABLE clinicalvisit_history (LIKE clinicalvisit);
CREATE TRIGGER versioning_trigger
BEFORE INSERT OR UPDATE OR DELETE ON clinicalvisit
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'clinicalvisit_history', true);
ALTER TABLE clinicalvisit_history OWNER TO sormas_user;

ALTER TABLE cases ADD COLUMN clinicalcourse_id bigint;
ALTER TABLE cases_history ADD COLUMN clinicalcourse_id bigint;
ALTER TABLE cases ADD CONSTRAINT fk_cases_clinicalcourse_id FOREIGN KEY (clinicalcourse_id) REFERENCES clinicalcourse (id);

ALTER TABLE symptoms ADD COLUMN bloodpressuresystolic integer;
ALTER TABLE symptoms ADD COLUMN bloodpressurediastolic integer;
ALTER TABLE symptoms ADD COLUMN heartrate integer;
ALTER TABLE symptoms_history ADD COLUMN bloodpressuresystolic integer;
ALTER TABLE symptoms_history ADD COLUMN bloodpressurediastolic integer;
ALTER TABLE symptoms_history ADD COLUMN heartrate integer;

INSERT INTO schema_version (version_number, comment) VALUES (127, 'Clinical course and visits #938');

-- 2019-02-13 Health conditions #952

CREATE TABLE healthconditions(
	id bigint not null,
	uuid varchar(36) not null unique,
	changedate timestamp not null,
	creationdate timestamp not null,
	tuberculosis varchar(255),
	asplenia varchar(255),
	hepatitis varchar(255),
	diabetes varchar(255),
	hiv varchar(255),
	hivart varchar(255),
	chronicliverdisease varchar(255),
	malignancychemotherapy varchar(255),
	chronicheartfailure varchar(255),
	chronicpulmonarydisease varchar(255),
	chronickidneydisease varchar(255),
	chronicneurologiccondition varchar(255),
	otherconditions varchar(512),
	sys_period tstzrange not null,
	primary key(id)
);
ALTER TABLE healthconditions OWNER TO sormas_user;

CREATE TABLE healthconditions_history (LIKE healthconditions);
CREATE TRIGGER versioning_trigger
BEFORE INSERT OR UPDATE OR DELETE ON healthconditions
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'healthconditions_history', true);
ALTER TABLE healthconditions_history OWNER TO sormas_user;

ALTER TABLE clinicalcourse ADD COLUMN healthconditions_id bigint;
ALTER TABLE clinicalcourse_history ADD COLUMN healthconditions_id bigint;
ALTER TABLE clinicalcourse ADD CONSTRAINT fk_clinicalcourse_healthconditions_id FOREIGN KEY (healthconditions_id) REFERENCES healthconditions (id);

INSERT INTO schema_version (version_number, comment) VALUES (128, 'Health conditions #952');

-- 2019-02-13 Additional Case and Person fields #935

ALTER TABLE cases ADD COLUMN sequelae varchar(255);
ALTER TABLE cases ADD COLUMN sequelaedetails varchar(512);

ALTER TABLE cases_history ADD COLUMN sequelae varchar(255);
ALTER TABLE cases_history ADD COLUMN sequelaedetails varchar(512);

ALTER TABLE person ADD COLUMN educationtype varchar(255);
ALTER TABLE person ADD COLUMN educationdetails varchar(255);
ALTER TABLE person ADD COLUMN approximateagereferencedate date;
UPDATE person SET approximateagereferencedate=changedate WHERE person.approximateage IS NOT NULL;

ALTER TABLE person_history ADD COLUMN educationtype varchar(255);
ALTER TABLE person_history ADD COLUMN educationdetails varchar(255);
ALTER TABLE person_history ADD COLUMN approximateagereferencedate date;

INSERT INTO schema_version (version_number, comment) VALUES (129, 'Additional Case and Person fields #935');

-- 2019-02-15 Additional Hospitalization fields and history table #935

ALTER TABLE hospitalization ADD COLUMN accommodation varchar(255);
ALTER TABLE hospitalization ADD COLUMN leftagainstadvice varchar(255);

ALTER TABLE hospitalization ADD COLUMN sys_period tstzrange;
UPDATE hospitalization SET sys_period=tstzrange(creationdate, null);
ALTER TABLE hospitalization ALTER COLUMN sys_period SET NOT NULL;
CREATE TABLE hospitalization_history (LIKE hospitalization);
CREATE TRIGGER versioning_trigger
BEFORE INSERT OR UPDATE OR DELETE ON hospitalization
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'hospitalization_history', true);
ALTER TABLE hospitalization_history OWNER TO sormas_user;

INSERT INTO schema_version (version_number, comment) VALUES (130, 'Additional Hospitalization fields and history table #935');

-- 2019-02-20 Additional signs and symptoms #938

ALTER TABLE symptoms ADD COLUMN pharyngealerythema varchar(255);
ALTER TABLE symptoms ADD COLUMN pharyngealexudate varchar(255);
ALTER TABLE symptoms ADD COLUMN oedemafaceneck varchar(255);
ALTER TABLE symptoms ADD COLUMN oedemalowerextremity varchar(255);
ALTER TABLE symptoms ADD COLUMN lossskinturgor varchar(255);
ALTER TABLE symptoms ADD COLUMN palpableliver varchar(255);
ALTER TABLE symptoms ADD COLUMN palpablespleen varchar(255);
ALTER TABLE symptoms ADD COLUMN malaise varchar(255);
ALTER TABLE symptoms ADD COLUMN sunkeneyesfontanelle varchar(255);
ALTER TABLE symptoms ADD COLUMN sidepain varchar(255);
ALTER TABLE symptoms ADD COLUMN fluidinlungcavity varchar(255);
ALTER TABLE symptoms ADD COLUMN tremor varchar(255);

ALTER TABLE symptoms_history ADD COLUMN pharyngealerythema varchar(255);
ALTER TABLE symptoms_history ADD COLUMN pharyngealexudate varchar(255);
ALTER TABLE symptoms_history ADD COLUMN oedemafaceneck varchar(255);
ALTER TABLE symptoms_history ADD COLUMN oedemalowerextremity varchar(255);
ALTER TABLE symptoms_history ADD COLUMN lossskinturgor varchar(255);
ALTER TABLE symptoms_history ADD COLUMN palpableliver varchar(255);
ALTER TABLE symptoms_history ADD COLUMN palpablespleen varchar(255);
ALTER TABLE symptoms_history ADD COLUMN malaise varchar(255);
ALTER TABLE symptoms_history ADD COLUMN sunkeneyesfontanelle varchar(255);
ALTER TABLE symptoms_history ADD COLUMN sidepain varchar(255);
ALTER TABLE symptoms_history ADD COLUMN fluidinlungcavity varchar(255);
ALTER TABLE symptoms_history ADD COLUMN tremor varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (131, 'Additional signs and symptoms #938');

-- 2019-02-18 Laboratory changes for case management #937

ALTER TABLE sampletest RENAME TO pathogentest;
ALTER TABLE sampletest_history RENAME TO pathogentest_history;

CREATE TABLE additionaltest(
	id bigint not null,
	uuid varchar(36) not null unique,
	changedate timestamp not null,
	creationdate timestamp not null,
	sample_id bigint not null,
	testdatetime timestamp not null,
	haemoglobinuria varchar(255),
	proteinuria varchar(255),
	hematuria varchar(255),
	arterialvenousgasph integer,
	arterialvenousgaspco2 integer,
	arterialvenousgaspao2 integer,
	arterialvenousgashco3 integer,
	gasoxygentherapy integer,
	altsgpt integer,
	astsgot integer,
	creatinine integer,
	potassium integer,
	urea integer,
	haemoglobin integer,
	totalbilirubin integer,
	conjbilirubin integer,
	wbccount integer,
	platelets integer,
	prothrombintime integer,
	othertestresults varchar(512),
	sys_period tstzrange not null,
	primary key(id));

ALTER TABLE additionaltest OWNER TO sormas_user;
ALTER TABLE additionaltest ADD CONSTRAINT fk_additionaltest_sample_id FOREIGN KEY (sample_id) REFERENCES samples (id);

CREATE TABLE additionaltest_history (LIKE additionaltest);
CREATE TRIGGER versioning_trigger
BEFORE INSERT OR UPDATE OR DELETE ON additionaltest
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'additionaltest_history', true);
ALTER TABLE additionaltest_history OWNER TO sormas_user;

ALTER TABLE samples ADD COLUMN pathogentestingrequested boolean;
ALTER TABLE samples_history ADD COLUMN pathogentestingrequested boolean;
ALTER TABLE samples ADD COLUMN additionaltestingrequested boolean;
ALTER TABLE samples_history ADD COLUMN additionaltestingrequested boolean;
ALTER TABLE samples ADD COLUMN requestedpathogentestsstring varchar(512);
ALTER TABLE samples_history ADD COLUMN requestedpathogentestsstring varchar(512);
ALTER TABLE samples ADD COLUMN requestedadditionaltestsstring varchar(512);
ALTER TABLE samples_history ADD COLUMN requestedadditionaltestsstring varchar(512);

UPDATE samples SET pathogentestingrequested = TRUE;
UPDATE samples SET additionaltestingrequested = FALSE;
UPDATE samples SET requestedpathogentestsstring = samples.suggestedtypeoftest;
ALTER TABLE samples DROP COLUMN suggestedtypeoftest;

INSERT INTO schema_version (version_number, comment) VALUES (132, 'Laboratory changes for case management #937');

-- 2019-03-06 Additions for Clinical Management #989

ALTER TABLE symptoms ADD COLUMN hemorrhagicsyndrome varchar(255);
ALTER TABLE symptoms ADD COLUMN hyperglycemia varchar(255);
ALTER TABLE symptoms ADD COLUMN hypoglycemia varchar(255);
ALTER TABLE symptoms ADD COLUMN sepsis varchar(255);
ALTER TABLE symptoms ADD COLUMN midupperarmcircumference integer;
ALTER TABLE symptoms ADD COLUMN respiratoryrate integer;
ALTER TABLE symptoms ADD COLUMN weight integer;
ALTER TABLE symptoms ADD COLUMN height integer;
ALTER TABLE symptoms ADD COLUMN glasgowcomascale integer;

ALTER TABLE symptoms_history ADD COLUMN hemorrhagicsyndrome varchar(255);
ALTER TABLE symptoms_history ADD COLUMN hyperglycemia varchar(255);
ALTER TABLE symptoms_history ADD COLUMN hypoglycemia varchar(255);
ALTER TABLE symptoms_history ADD COLUMN sepsis varchar(255);
ALTER TABLE symptoms_history ADD COLUMN midupperarmcircumference integer;
ALTER TABLE symptoms_history ADD COLUMN respiratoryrate integer;
ALTER TABLE symptoms_history ADD COLUMN weight integer;
ALTER TABLE symptoms_history ADD COLUMN height integer;
ALTER TABLE symptoms_history ADD COLUMN glasgowcomascale integer;

INSERT INTO schema_version (version_number, comment) VALUES (133, 'Additions for Clinical Management #989');

-- 2019-03-01 Add pathogen test result to sample #919

ALTER TABLE samples ADD COLUMN pathogentestresult varchar(255);
ALTER TABLE samples_history ADD COLUMN pathogentestresult varchar(255);
UPDATE samples SET pathogentestresult = testresult FROM pathogentest WHERE pathogentest.id = samples.mainsampletest_id;
UPDATE samples SET pathogentestresult = 'PENDING' WHERE pathogentestresult IS NULL AND pathogentestingrequested = TRUE;
UPDATE samples SET changedate = now() WHERE pathogentestresult IS NOT NULL;
ALTER TABLE samples DROP COLUMN mainsampletest_id;
ALTER TABLE samples_history DROP COLUMN mainsampletest_id;

DROP TRIGGER versioning_trigger ON pathogentest;
CREATE TRIGGER versioning_trigger
BEFORE INSERT OR UPDATE OR DELETE ON pathogentest
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'pathogentest_history', true);

INSERT INTO schema_version (version_number, comment) VALUES (134, 'Add pathogen test result to sample #919');

-- 2019-03-08 Further additions for clinical management #993

UPDATE symptoms SET weight = weight * 100 WHERE weight IS NOT NULL;
UPDATE symptoms SET midupperarmcircumference = midupperarmcircumference * 100 WHERE midupperarmcircumference IS NOT NULL;
UPDATE symptoms SET changedate = now() WHERE weight IS NOT NULL OR midupperarmcircumference IS NOT NULL;
ALTER TABLE samples ADD COLUMN requestedotherpathogentests varchar(512);
ALTER TABLE samples_history ADD COLUMN requestedotherpathogentests varchar(512);
ALTER TABLE samples ADD COLUMN requestedotheradditionaltests varchar(512);
ALTER TABLE samples_history ADD COLUMN requestedotheradditionaltests varchar(512);

INSERT INTO schema_version (version_number, comment) VALUES (135, 'Further additions for clinical management #993');

-- 2019-03-15 Delete duplicate weekly reports #994

ALTER TABLE weeklyreportentry DROP CONSTRAINT fk_weeklyreportentry_weeklyreport_id;
ALTER TABLE weeklyreportentry ADD CONSTRAINT fk_weeklyreportentry_weeklyreport_id FOREIGN KEY (weeklyreport_id) REFERENCES public.weeklyreport (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE CASCADE;
DELETE FROM weeklyreport a USING weeklyreport b WHERE a.id < b.id AND a.year = b.year AND a.epiweek = b.epiweek AND a.reportinguser_id = b.reportinguser_id;

INSERT INTO schema_version (version_number, comment) VALUES (136, 'Delete duplicate weekly reports #994');

-- 2019-03-28 Add therapies to all cases

INSERT INTO schema_version (version_number, comment) VALUES (137, 'Add therapies to all cases');

-- 2019-03-28 Change serialization of approximate age type to string (instead of number) #1015

ALTER TABLE person DISABLE TRIGGER versioning_trigger;
ALTER TABLE person RENAME approximateagetype TO approximateagetype_temp;
ALTER TABLE person_history RENAME approximateagetype TO approximateagetype_temp;
ALTER TABLE person ADD COLUMN approximateagetype character varying(255);
ALTER TABLE person_history ADD COLUMN approximateagetype character varying(255);
UPDATE person SET approximateagetype='YEARS' WHERE approximateagetype_temp=0;
UPDATE person SET approximateagetype='MONTHS' WHERE approximateagetype_temp=1;
UPDATE person SET approximateagetype='DAYS' WHERE approximateagetype_temp=2;
UPDATE person_history SET approximateagetype='YEARS' WHERE approximateagetype_temp=0;
UPDATE person_history SET approximateagetype='MONTHS' WHERE approximateagetype_temp=1;
UPDATE person_history SET approximateagetype='DAYS' WHERE approximateagetype_temp=2;
ALTER TABLE person DROP COLUMN approximateagetype_temp;
ALTER TABLE person_history DROP COLUMN approximateagetype_temp;
ALTER TABLE person ENABLE TRIGGER versioning_trigger;

INSERT INTO schema_version (version_number, comment) VALUES (138, 'Change serialization of approximate age type to string (instead of number) #1015');

-- 2019-04-05 Replace creation of therapies/clinical courses in StartupShutdownService with SQL script #1042

DO $$
DECLARE rec RECORD;
DECLARE new_therapy_id INTEGER;
DECLARE new_clinical_course_id INTEGER;
DECLARE new_health_conditions_id INTEGER;
BEGIN
FOR rec IN SELECT id FROM public.cases WHERE therapy_id IS NULL
LOOP
INSERT INTO therapy(id, uuid, creationdate, changedate) VALUES (nextval('entity_seq'), upper(substring(CAST(CAST(md5(CAST(random() AS text) || CAST(clock_timestamp() AS text)) AS uuid) AS text), 3, 29)), now(), now()) RETURNING id INTO new_therapy_id;
UPDATE cases SET therapy_id = new_therapy_id WHERE id = rec.id;
END LOOP;
FOR rec IN SELECT id FROM public.cases WHERE clinicalcourse_id IS NULL
LOOP
INSERT INTO healthconditions(id, uuid, creationdate, changedate) VALUES (nextval('entity_seq'), upper(substring(CAST(CAST(md5(CAST(random() AS text) || CAST(clock_timestamp() AS text)) AS uuid) AS text), 3, 29)), now(), now()) RETURNING id INTO new_health_conditions_id;
INSERT INTO clinicalcourse(id, uuid, creationdate, changedate, healthconditions_id) VALUES (nextval('entity_seq'), upper(substring(CAST(CAST(md5(CAST(random() AS text) || CAST(clock_timestamp() AS text)) AS uuid) AS text), 3, 29)), now(), now(), new_health_conditions_id) RETURNING id INTO new_clinical_course_id;
UPDATE cases SET clinicalcourse_id = new_clinical_course_id WHERE id = rec.id;
END LOOP;
END;
$$ LANGUAGE plpgsql;

INSERT INTO schema_version (version_number, comment) VALUES (139, 'Replace creation of therapies/clinical courses in StartupShutdownService with SQL script #1042');

-- 2019-04-11 Drop person_id from clinical visits #1005

ALTER TABLE clinicalvisit DROP COLUMN person_id;

INSERT INTO schema_version (version_number, comment) VALUES (140, 'Drop person_id from clinical visits #1005');

-- 2019-04-12 Create potentially missing therapies and/or clinical courses #1042

DO $$
DECLARE rec RECORD;
DECLARE new_therapy_id INTEGER;
DECLARE new_clinical_course_id INTEGER;
DECLARE new_health_conditions_id INTEGER;
BEGIN
FOR rec IN SELECT id FROM public.cases WHERE therapy_id IS NULL
LOOP
INSERT INTO therapy(id, uuid, creationdate, changedate) VALUES (nextval('entity_seq'), upper(substring(CAST(CAST(md5(CAST(random() AS text) || CAST(clock_timestamp() AS text)) AS uuid) AS text), 3, 29)), now(), now()) RETURNING id INTO new_therapy_id;
UPDATE cases SET therapy_id = new_therapy_id WHERE id = rec.id;
END LOOP;
FOR rec IN SELECT id FROM public.cases WHERE clinicalcourse_id IS NULL
LOOP
INSERT INTO healthconditions(id, uuid, creationdate, changedate) VALUES (nextval('entity_seq'), upper(substring(CAST(CAST(md5(CAST(random() AS text) || CAST(clock_timestamp() AS text)) AS uuid) AS text), 3, 29)), now(), now()) RETURNING id INTO new_health_conditions_id;
INSERT INTO clinicalcourse(id, uuid, creationdate, changedate, healthconditions_id) VALUES (nextval('entity_seq'), upper(substring(CAST(CAST(md5(CAST(random() AS text) || CAST(clock_timestamp() AS text)) AS uuid) AS text), 3, 29)), now(), now(), new_health_conditions_id) RETURNING id INTO new_clinical_course_id;
UPDATE cases SET clinicalcourse_id = new_clinical_course_id WHERE id = rec.id;
END LOOP;
END;
$$ LANGUAGE plpgsql;

INSERT INTO schema_version (version_number, comment) VALUES (141, 'Create potentially missing therapies and/or clinical courses #1042');

-- 2019-04-15 Adjust export_database_join function to not include cases #1016

DROP FUNCTION export_database_join(text, text, text, text, text);

CREATE FUNCTION export_database_join(table_name text, join_table_name text, column_name text, join_column_name text, file_path text)
	RETURNS VOID
	LANGUAGE plpgsql
	SECURITY DEFINER
	AS $BODY$
		BEGIN
			EXECUTE '
				COPY (SELECT
					' || quote_ident(table_name) || '
				.* FROM
					' || quote_ident(table_name) || '
				INNER JOIN
					' || quote_ident(join_table_name) || '
				ON
					' || column_name || '
				=
					' || join_column_name || '
				) TO
					' || quote_literal(file_path) || '
				WITH (
					FORMAT CSV, DELIMITER '';'', HEADER
				);
			';
		END;
	$BODY$
;

INSERT INTO schema_version (version_number, comment) VALUES (142, 'Adjust export_database_join function to not include cases #1016');

-- 2019-04-15 Added missing foreign key constraint to events surveillance officer

ALTER TABLE events ADD CONSTRAINT fk_events_surveillanceofficer_id FOREIGN KEY (surveillanceofficer_id) REFERENCES users (id);

INSERT INTO schema_version (version_number, comment) VALUES (143, 'Added missing foreign key constraint to events surveillance officer');

-- 2019-04-23 Rename "aktiv" to "active" in user table

ALTER TABLE users RENAME COLUMN aktiv TO active;
ALTER TABLE users_history RENAME COLUMN aktiv TO active;

INSERT INTO schema_version (version_number, comment) VALUES (144, 'Rename aktiv to active in user table');

-- 2019-04-24 Add DiseaseConfiguration entity #1074

CREATE TABLE diseaseconfiguration(
	id bigint not null,
	uuid varchar(36) not null unique,
	changedate timestamp not null,
	creationdate timestamp not null,
	disease varchar(255),
	active boolean,
	primarydisease boolean,
	followupenabled boolean,
	followupduration integer,
	sys_period tstzrange not null,
	primary key(id));

ALTER TABLE diseaseconfiguration OWNER TO sormas_user;

CREATE TABLE diseaseconfiguration_history (LIKE diseaseconfiguration);
CREATE TRIGGER versioning_trigger
BEFORE INSERT OR UPDATE OR DELETE ON diseaseconfiguration
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'diseaseconfiguration_history', true);
ALTER TABLE diseaseconfiguration_history OWNER TO sormas_user;

INSERT INTO schema_version (version_number, comment) VALUES (145, 'Add DiseaseConfiguration entity #1074');

-- 2019-04-29 Rename education "nursery" to "no education" #1073
UPDATE person SET educationtype='NONE' WHERE educationtype='NURSERY';
UPDATE person_history SET educationtype='NONE' WHERE educationtype='NURSERY';
INSERT INTO schema_version (version_number, comment) VALUES (146, 'Rename education "nursery" to "no education" #1073');

-- 2019-04-29 Merge event type and status #1077
ALTER TABLE events DROP COLUMN eventtype;
ALTER TABLE events_history DROP COLUMN eventtype;
INSERT INTO schema_version (version_number, comment) VALUES (147, 'Merge event type and status #1077');

-- 2019-04-26 Add tested disease to PathogenTests #1075

ALTER TABLE pathogentest ADD COLUMN testeddisease varchar(255);
ALTER TABLE pathogentest_history ADD COLUMN testeddisease varchar(255);
ALTER TABLE pathogentest ADD COLUMN testeddiseasedetails varchar(512);
ALTER TABLE pathogentest_history ADD COLUMN testeddiseasedetails varchar(512);
UPDATE pathogentest SET testeddisease = 'WEST_NILE_FEVER' WHERE testtype = 'WEST_NILE_FEVER_IGM' OR testtype = 'WEST_NILE_FEVER_ANTIBODIES';
UPDATE pathogentest SET testeddisease = 'DENGUE' WHERE testtype = 'DENGUE_FEVER_IGM' OR testtype = 'DENGUE_FEVER_ANTIBODIES';
UPDATE pathogentest SET testeddisease = 'YELLOW_FEVER' WHERE testtype = 'YELLOW_FEVER_IGM' OR testtype = 'YELLOW_FEVER_ANTIBODIES';
UPDATE pathogentest SET testeddisease = 'PLAGUE' WHERE testtype = 'YERSINIA_PESTIS_ANTIGEN';
UPDATE pathogentest SET testeddisease = (SELECT disease FROM cases WHERE cases.id = (SELECT associatedcase_id FROM samples WHERE samples.id = pathogentest.sample_id)) WHERE testeddisease IS NULL;
UPDATE pathogentest SET testtype = 'IGM_SERUM_ANTIBODY' WHERE testtype = 'DENGUE_FEVER_IGM' OR testtype = 'WEST_NILE_FEVER_IGM' OR testtype = 'YELLOW_FEVER_IGM';
UPDATE pathogentest SET testtype = 'NEUTRALIZING_ANTIBODIES' WHERE testtype = 'DENGUE_FEVER_ANTIBODIES' OR testtype = 'WEST_NILE_FEVER_ANTIBODIES' OR testtype = 'YELLOW_FEVER_ANTIBODIES';
UPDATE pathogentest SET testtype = 'ANTIGEN_DETECTION' WHERE testtype = 'YERSINIA_PESTIS_ANTIGEN';
UPDATE samples SET requestedpathogentestsstring = REPLACE(requestedpathogentestsstring, 'DENGUE_FEVER_IGM', 'IGM_SERUM_ANTIBODY');
UPDATE samples SET requestedpathogentestsstring = REPLACE(requestedpathogentestsstring, 'WEST_NILE_FEVER_IGM', 'IGM_SERUM_ANTIBODY');
UPDATE samples SET requestedpathogentestsstring = REPLACE(requestedpathogentestsstring, 'YELLOW_FEVER_IGM', 'IGM_SERUM_ANTIBODY');
UPDATE samples SET requestedpathogentestsstring = REPLACE(requestedpathogentestsstring, 'DENGUE_FEVER_ANTIBODIES', 'NEUTRALIZING_ANTIBODIES');
UPDATE samples SET requestedpathogentestsstring = REPLACE(requestedpathogentestsstring, 'WEST_NILE_FEVER_ANTIBODIES', 'NEUTRALIZING_ANTIBODIES');
UPDATE samples SET requestedpathogentestsstring = REPLACE(requestedpathogentestsstring, 'YELLOW_FEVER_ANTIBODIES', 'NEUTRALIZING_ANTIBODIES');
UPDATE samples SET requestedpathogentestsstring = REPLACE(requestedpathogentestsstring, 'YERSINIA_PESTIS_ANTIGEN', 'ANTIGEN_DETECTION');

INSERT INTO schema_version (version_number, comment) VALUES (148, 'Add tested disease to PathogenTests #1075');

-- 2019-04-29 Rename "no education" back to "nursery" #1073
UPDATE person SET educationtype='NURSERY' WHERE educationtype='NONE';
UPDATE person_history SET educationtype='NURSERY' WHERE educationtype='NONE';
INSERT INTO schema_version (version_number, comment) VALUES (149, 'Rename "no education" back to "nursery" #1073');

-- 2019-05-09 Add new fields for Yellow fever and Measles #1088
ALTER TABLE location ADD COLUMN areatype varchar(255);
ALTER TABLE location_history ADD COLUMN areatype varchar(255);
ALTER TABLE cases ADD COLUMN regionleveldate timestamp;
ALTER TABLE cases_history ADD COLUMN regionleveldate timestamp;
ALTER TABLE cases ADD COLUMN nationalleveldate timestamp;
ALTER TABLE cases_history ADD COLUMN nationalleveldate timestamp;
ALTER TABLE cases RENAME receptiondate TO districtleveldate;
ALTER TABLE cases_history RENAME receptiondate TO districtleveldate;
ALTER TABLE cases ADD COLUMN cliniciandetails varchar(512);
ALTER TABLE cases_history ADD COLUMN cliniciandetails varchar(512);

INSERT INTO schema_version (version_number, comment) VALUES (150, 'Add new fields for Yellow fever and Measles #1088');

-- 2019-05-20 Add new fields for Congenital Rubella #1133
ALTER TABLE users ADD COLUMN limiteddisease varchar(255);
ALTER TABLE users_history ADD COLUMN limiteddisease varchar(255);
ALTER TABLE symptoms ADD COLUMN bilateralcataracts varchar(255);
ALTER TABLE symptoms ADD COLUMN unilateralcataracts varchar(255);
ALTER TABLE symptoms ADD COLUMN congenitalglaucoma varchar(255);
ALTER TABLE symptoms ADD COLUMN pigmentaryretinopathy varchar(255);
ALTER TABLE symptoms ADD COLUMN purpuricrash varchar(255);
ALTER TABLE symptoms ADD COLUMN microcephaly varchar(255);
ALTER TABLE symptoms ADD COLUMN developmentaldelay varchar(255);
ALTER TABLE symptoms ADD COLUMN splenomegaly varchar(255);
ALTER TABLE symptoms ADD COLUMN meningoencephalitis varchar(255);
ALTER TABLE symptoms ADD COLUMN radiolucentbonedisease varchar(255);
ALTER TABLE symptoms ADD COLUMN congenitalheartdisease varchar(255);
ALTER TABLE symptoms ADD COLUMN congenitalheartdiseasetype varchar(255);
ALTER TABLE symptoms ADD COLUMN congenitalheartdiseasedetails varchar(512);
ALTER TABLE symptoms ADD COLUMN jaundicewithin24hoursofbirth varchar(255);
ALTER TABLE symptoms_history ADD COLUMN bilateralcataracts varchar(255);
ALTER TABLE symptoms_history ADD COLUMN unilateralcataracts varchar(255);
ALTER TABLE symptoms_history ADD COLUMN congenitalglaucoma varchar(255);
ALTER TABLE symptoms_history ADD COLUMN pigmentaryretinopathy varchar(255);
ALTER TABLE symptoms_history ADD COLUMN purpuricrash varchar(255);
ALTER TABLE symptoms_history ADD COLUMN microcephaly varchar(255);
ALTER TABLE symptoms_history ADD COLUMN developmentaldelay varchar(255);
ALTER TABLE symptoms_history ADD COLUMN splenomegaly varchar(255);
ALTER TABLE symptoms_history ADD COLUMN meningoencephalitis varchar(255);
ALTER TABLE symptoms_history ADD COLUMN radiolucentbonedisease varchar(255);
ALTER TABLE symptoms_history ADD COLUMN congenitalheartdisease varchar(255);
ALTER TABLE symptoms_history ADD COLUMN congenitalheartdiseasetype varchar(255);
ALTER TABLE symptoms_history ADD COLUMN congenitalheartdiseasedetails varchar(512);
ALTER TABLE symptoms_history ADD COLUMN jaundicewithin24hoursofbirth varchar(255);
ALTER TABLE healthconditions ADD COLUMN downsyndrome varchar(255);
ALTER TABLE healthconditions ADD COLUMN congenitalsyphilis varchar(255);
ALTER TABLE healthconditions_history ADD COLUMN downsyndrome varchar(255);
ALTER TABLE healthconditions_history ADD COLUMN congenitalsyphilis varchar(255);
ALTER TABLE cases ADD COLUMN notifyingclinic varchar(255);
ALTER TABLE cases ADD COLUMN notifyingclinicdetails varchar(512);
ALTER TABLE cases_history ADD COLUMN notifyingclinic varchar(255);
ALTER TABLE cases_history ADD COLUMN notifyingclinicdetails varchar(512);

CREATE TABLE maternalhistory(
	id bigint not null,
	uuid varchar(36) not null unique,
	changedate timestamp not null,
	creationdate timestamp not null,
	childrennumber integer,
	ageatbirth integer,
	conjunctivitis varchar(255),
	conjunctivitisonset timestamp,
	conjunctivitismonth integer,
	maculopapularrash varchar(255),
	maculopapularrashonset timestamp,
	maculopapularrashmonth integer,
	swollenlymphs varchar(255),
	swollenlymphsonset timestamp,
	swollenlymphsmonth integer,
	arthralgiaarthritis varchar(255),
	arthralgiaarthritisonset timestamp,
	arthralgiaarthritismonth integer,
	othercomplications varchar(255),
	othercomplicationsonset timestamp,
	othercomplicationsmonth integer,
	othercomplicationsdetails varchar(512),
	rubella varchar(255),
	rubellaonset timestamp,
	rashexposure varchar(255),
	rashexposuredate timestamp,
	rashexposuremonth integer,
	rashexposureregion_id bigint,
	rashexposuredistrict_id bigint,
	rashexposurecommunity_id bigint,
	sys_period tstzrange not null,
	primary key(id)
);
ALTER TABLE maternalhistory OWNER TO sormas_user;
ALTER TABLE maternalhistory ADD CONSTRAINT fk_maternalhistory_rashexposureregion_id FOREIGN KEY (rashexposureregion_id) REFERENCES region (id);
ALTER TABLE maternalhistory ADD CONSTRAINT fk_maternalhistory_rashexposuredistrict_id FOREIGN KEY (rashexposuredistrict_id) REFERENCES district (id);
ALTER TABLE maternalhistory ADD CONSTRAINT fk_maternalhistory_rashexposurecommunity_id FOREIGN KEY (rashexposurecommunity_id) REFERENCES community (id);

CREATE TABLE maternalhistory_history (LIKE maternalhistory);
CREATE TRIGGER versioning_trigger
BEFORE INSERT OR UPDATE OR DELETE ON maternalhistory
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'maternalhistory_history', true);
ALTER TABLE maternalhistory_history OWNER TO sormas_user;

ALTER TABLE cases ADD COLUMN maternalhistory_id bigint;
ALTER TABLE cases_history ADD COLUMN maternalhistory_id bigint;
ALTER TABLE cases ADD CONSTRAINT fk_cases_maternalhistory_id FOREIGN KEY (maternalhistory_id) REFERENCES maternalhistory (id);

DO $$
DECLARE rec RECORD;
DECLARE new_maternalhistory_id INTEGER;
BEGIN
FOR rec IN SELECT id FROM public.cases WHERE maternalhistory_id IS NULL
LOOP
INSERT INTO maternalhistory(id, uuid, creationdate, changedate) VALUES (nextval('entity_seq'), upper(substring(CAST(CAST(md5(CAST(random() AS text) || CAST(clock_timestamp() AS text)) AS uuid) AS text), 3, 29)), now(), now()) RETURNING id INTO new_maternalhistory_id;
UPDATE cases SET maternalhistory_id = new_maternalhistory_id WHERE id = rec.id;
END LOOP;
END;
$$ LANGUAGE plpgsql;

ALTER TABLE person ADD COLUMN mothersname varchar(512);
ALTER TABLE person ADD COLUMN fathersname varchar(512);
ALTER TABLE person ADD COLUMN placeofbirthregion_id bigint;
ALTER TABLE person ADD COLUMN placeofbirthdistrict_id bigint;
ALTER TABLE person ADD COLUMN placeofbirthcommunity_id bigint;
ALTER TABLE person ADD COLUMN placeofbirthfacility_id bigint;
ALTER TABLE person ADD COLUMN placeofbirthfacilitydetails varchar(512);
ALTER TABLE person ADD COLUMN gestationageatbirth integer;
ALTER TABLE person ADD COLUMN birthweight integer;
ALTER TABLE person_history ADD COLUMN mothersname varchar(512);
ALTER TABLE person_history ADD COLUMN fathersname varchar(512);
ALTER TABLE person_history ADD COLUMN placeofbirthregion_id bigint;
ALTER TABLE person_history ADD COLUMN placeofbirthdistrict_id bigint;
ALTER TABLE person_history ADD COLUMN placeofbirthcommunity_id bigint;
ALTER TABLE person_history ADD COLUMN placeofbirthfacility_id bigint;
ALTER TABLE person_history ADD COLUMN placeofbirthfacilitydetails varchar(512);
ALTER TABLE person_history ADD COLUMN gestationageatbirth integer;
ALTER TABLE person_history ADD COLUMN birthweight integer;

ALTER TABLE person ADD CONSTRAINT fk_person_placeofbirthregion_id FOREIGN KEY (placeofbirthregion_id) REFERENCES region (id);
ALTER TABLE person ADD CONSTRAINT fk_person_placeofbirthdistrict_id FOREIGN KEY (placeofbirthdistrict_id) REFERENCES district (id);
ALTER TABLE person ADD CONSTRAINT fk_person_placeofbirthcommunity_id FOREIGN KEY (placeofbirthcommunity_id) REFERENCES community (id);
ALTER TABLE person ADD CONSTRAINT fk_person_placeofbirthfacility_id FOREIGN KEY (placeofbirthfacility_id) REFERENCES facility (id);

INSERT INTO schema_version (version_number, comment) VALUES (151, 'Add new fields for Congenital Rubella #1133');

-- 2019-06-25 Add version case was created #1106
ALTER TABLE cases ADD COLUMN versioncreated varchar(32);

INSERT INTO schema_version (version_number, comment) VALUES (152, 'Add version case was created #1106');

-- 2019-07-01 Fixed problems in database schema #1198
UPDATE public.location SET areatype='URBAN' WHERE areatype='0';
UPDATE public.location SET areatype='RURAL' WHERE areatype='1';

UPDATE public.symptoms SET congenitalGlaucoma='YES' WHERE congenitalGlaucoma='0';
UPDATE public.symptoms SET congenitalGlaucoma='NO' WHERE congenitalGlaucoma='1';
UPDATE public.symptoms SET congenitalGlaucoma='UNKNOWN' WHERE congenitalGlaucoma='2';

UPDATE public.symptoms SET lesionsResembleImg1='YES' WHERE lesionsResembleImg1='0';
UPDATE public.symptoms SET lesionsResembleImg1='NO' WHERE lesionsResembleImg1='1';
UPDATE public.symptoms SET lesionsResembleImg1='UNKNOWN' WHERE lesionsResembleImg1='2';
UPDATE public.symptoms SET lesionsResembleImg2='YES' WHERE lesionsResembleImg2='0';
UPDATE public.symptoms SET lesionsResembleImg2='NO' WHERE lesionsResembleImg2='1';
UPDATE public.symptoms SET lesionsResembleImg2='UNKNOWN' WHERE lesionsResembleImg2='2';
UPDATE public.symptoms SET lesionsResembleImg3='YES' WHERE lesionsResembleImg3='0';
UPDATE public.symptoms SET lesionsResembleImg3='NO' WHERE lesionsResembleImg3='1';
UPDATE public.symptoms SET lesionsResembleImg3='UNKNOWN' WHERE lesionsResembleImg3='2';
UPDATE public.symptoms SET lesionsResembleImg4='YES' WHERE lesionsResembleImg4='0';
UPDATE public.symptoms SET lesionsResembleImg4='NO' WHERE lesionsResembleImg4='1';
UPDATE public.symptoms SET lesionsResembleImg4='UNKNOWN' WHERE lesionsResembleImg4='2';

INSERT INTO schema_version (version_number, comment) VALUES (153, 'Fixed problems in database schema #1198');

-- 2019-07-02 Renamed version case was created #1106
ALTER TABLE cases RENAME COLUMN versioncreated TO creationversion;

INSERT INTO schema_version (version_number, comment) VALUES (154, 'Renamed version case was created #1106');

-- 2019-07-03 Added missing not null to publicownership #1198
UPDATE public.facility SET publicownership=false WHERE publicownership IS NULL;
ALTER TABLE public.facility ALTER COLUMN publicownership SET NOT NULL;

INSERT INTO schema_version (version_number, comment) VALUES (155, 'Added missing not null to publicownership #1198');

-- 2019-06-28 Add fields and tables for points of entry and port health info #985
CREATE TABLE pointofentry(
	id bigint not null,
	uuid varchar(36) not null unique,
	changedate timestamp not null,
	creationdate timestamp not null,
	pointofentrytype varchar(255),
	name varchar(512),
	region_id bigint,
	district_id bigint,
	latitude double precision,
	longitude double precision,
	active boolean,
	primary key(id)
);
ALTER TABLE pointofentry OWNER TO sormas_user;
ALTER TABLE pointofentry ADD CONSTRAINT fk_pointofentry_region_id FOREIGN KEY (region_id) REFERENCES region (id);
ALTER TABLE pointofentry ADD CONSTRAINT fk_pointofentry_district_id FOREIGN KEY (district_id) REFERENCES district (id);

ALTER TABLE cases ADD COLUMN caseorigin varchar(255);
ALTER TABLE cases ADD COLUMN pointofentry_id bigint;
ALTER TABLE cases ADD COLUMN pointofentrydetails varchar(512);
ALTER TABLE cases_history ADD COLUMN pointofentry_id bigint;
ALTER TABLE cases_history ADD COLUMN pointofentrydetails varchar(512);
ALTER TABLE cases_history ADD COLUMN caseorigin varchar(255);
UPDATE cases SET caseorigin = 'IN_COUNTRY';

ALTER TABLE cases ADD CONSTRAINT fk_cases_pointofentry_id FOREIGN KEY (pointofentry_id) REFERENCES pointofentry (id);

ALTER TABLE users ADD COLUMN pointofentry_id bigint;
ALTER TABLE users_history ADD COLUMN pointofentry_id bigint;

ALTER TABLE users ADD CONSTRAINT fk_users_pointofentry_id FOREIGN KEY (pointofentry_id) REFERENCES pointofentry (id);

CREATE TABLE porthealthinfo(
	id bigint not null,
	uuid varchar(36) not null unique,
	changedate timestamp not null,
	creationdate timestamp not null,
	airlinename varchar(512),
	flightnumber varchar(512),
	departuredatetime timestamp,
	arrivaldatetime timestamp,
	freeseating varchar(255),
	seatnumber varchar(512),
	departureairport varchar(512),
	numberoftransitstops integer,
	transitstopdetails1 varchar(512),
	transitstopdetails2 varchar(512),
	transitstopdetails3 varchar(512),
	transitstopdetails4 varchar(512),
	transitstopdetails5 varchar(512),
	vesselname varchar(512),
	vesseldetails varchar(512),
	portofdeparture varchar(512),
	lastportofcall varchar(512),
	conveyancetype varchar(255),
	conveyancetypedetails varchar(512),
	departurelocation varchar(512),
	finaldestination varchar(512),
	details varchar(512),
	sys_period tstzrange not null,
	primary key(id)
);

ALTER TABLE porthealthinfo OWNER TO sormas_user;

CREATE TABLE porthealthinfo_history (LIKE porthealthinfo);
CREATE TRIGGER versioning_trigger
BEFORE INSERT OR UPDATE OR DELETE ON porthealthinfo
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'porthealthinfo_history', true);
ALTER TABLE porthealthinfo_history OWNER TO sormas_user;

ALTER TABLE cases ADD COLUMN porthealthinfo_id bigint;
ALTER TABLE cases_history ADD COLUMN porthealthinfo_id bigint;
ALTER TABLE cases ADD CONSTRAINT fk_cases_porthealthinfo_id FOREIGN KEY (porthealthinfo_id) REFERENCES porthealthinfo (id);

INSERT INTO schema_version (version_number, comment) VALUES (156, 'Add fields and tables for points of entry and port health info #985');

-- 2019-07-16 Change types of AdditionalTest columns to double #1200
ALTER TABLE additionaltest ALTER COLUMN arterialvenousgasph TYPE real;
ALTER TABLE additionaltest ALTER COLUMN arterialvenousgaspco2 TYPE real;
ALTER TABLE additionaltest ALTER COLUMN arterialvenousgaspao2 TYPE real;
ALTER TABLE additionaltest ALTER COLUMN arterialvenousgashco3 TYPE real;
ALTER TABLE additionaltest ALTER COLUMN gasoxygentherapy TYPE real;
ALTER TABLE additionaltest ALTER COLUMN altsgpt TYPE real;
ALTER TABLE additionaltest ALTER COLUMN astsgot TYPE real;
ALTER TABLE additionaltest ALTER COLUMN creatinine TYPE real;
ALTER TABLE additionaltest ALTER COLUMN potassium TYPE real;
ALTER TABLE additionaltest ALTER COLUMN urea TYPE real;
ALTER TABLE additionaltest ALTER COLUMN haemoglobin TYPE real;
ALTER TABLE additionaltest ALTER COLUMN totalbilirubin TYPE real;
ALTER TABLE additionaltest ALTER COLUMN conjbilirubin TYPE real;
ALTER TABLE additionaltest ALTER COLUMN wbccount TYPE real;
ALTER TABLE additionaltest ALTER COLUMN platelets TYPE real;
ALTER TABLE additionaltest ALTER COLUMN prothrombintime TYPE real;

ALTER TABLE additionaltest_history ALTER COLUMN arterialvenousgasph TYPE real;
ALTER TABLE additionaltest_history ALTER COLUMN arterialvenousgaspco2 TYPE real;
ALTER TABLE additionaltest_history ALTER COLUMN arterialvenousgaspao2 TYPE real;
ALTER TABLE additionaltest_history ALTER COLUMN arterialvenousgashco3 TYPE real;
ALTER TABLE additionaltest_history ALTER COLUMN gasoxygentherapy TYPE real;
ALTER TABLE additionaltest_history ALTER COLUMN altsgpt TYPE real;
ALTER TABLE additionaltest_history ALTER COLUMN astsgot TYPE real;
ALTER TABLE additionaltest_history ALTER COLUMN creatinine TYPE real;
ALTER TABLE additionaltest_history ALTER COLUMN potassium TYPE real;
ALTER TABLE additionaltest_history ALTER COLUMN urea TYPE real;
ALTER TABLE additionaltest_history ALTER COLUMN haemoglobin TYPE real;
ALTER TABLE additionaltest_history ALTER COLUMN totalbilirubin TYPE real;
ALTER TABLE additionaltest_history ALTER COLUMN conjbilirubin TYPE real;
ALTER TABLE additionaltest_history ALTER COLUMN wbccount TYPE real;
ALTER TABLE additionaltest_history ALTER COLUMN platelets TYPE real;
ALTER TABLE additionaltest_history ALTER COLUMN prothrombintime TYPE real;

INSERT INTO schema_version (version_number, comment) VALUES (157, 'Change types of AdditionalTest columns to double #1200');

-- 2019-07-19 Add clinician phone and email #1190
ALTER TABLE cases ADD COLUMN clinicianphone varchar(512);
ALTER TABLE cases ADD COLUMN clinicianemail varchar(512);
ALTER TABLE cases_history ADD COLUMN clinicianphone varchar(512);
ALTER TABLE cases_history ADD COLUMN clinicianemail varchar(512);

INSERT INTO schema_version (version_number, comment) VALUES (158, 'Add clinician phone and email #1190');

-- 2019-07-19 Fix Hibernate "feature" that throws an error when using functions without a return value #1228
DROP FUNCTION export_database(text, text);
DROP FUNCTION export_database_join(text, text, text, text, text);

CREATE FUNCTION export_database(table_name text, file_path text)
	RETURNS INTEGER
	LANGUAGE plpgsql
	SECURITY DEFINER
	AS $BODY$
		BEGIN
			EXECUTE '
				COPY (SELECT * FROM
					' || quote_ident(table_name) || '
				) TO
					' || quote_literal(file_path) || '
				WITH (
					FORMAT CSV, DELIMITER '';'', HEADER
				);
			';
			RETURN 1;
		END;
	$BODY$
;

CREATE FUNCTION export_database_join(table_name text, join_table_name text, column_name text, join_column_name text, file_path text)
	RETURNS INTEGER
	LANGUAGE plpgsql
	SECURITY DEFINER
	AS $BODY$
		BEGIN
			EXECUTE '
				COPY (SELECT * FROM
					' || quote_ident(table_name) || '
				INNER JOIN
					' || quote_ident(join_table_name) || '
				ON
					' || column_name || '
				=
					' || join_column_name || '
				) TO
					' || quote_literal(file_path) || '
				WITH (
					FORMAT CSV, DELIMITER '';'', HEADER
				);
			';
			RETURN 1;
		END;
	$BODY$
;

INSERT INTO schema_version (version_number, comment) VALUES (159, 'Fix Hibernate "feature" that throws an error when using functions without a return value #1228');

-- 2019-08-02 Add duplicateOf column to case #1232
ALTER TABLE cases ADD COLUMN duplicateof_id bigint;
ALTER TABLE cases_history ADD COLUMN duplicateof_id bigint;

ALTER TABLE cases ADD CONSTRAINT fk_cases_duplicateof_id FOREIGN KEY (duplicateof_id) REFERENCES cases(id);

INSERT INTO schema_version (version_number, comment) VALUES (160, 'Add duplicateOf column to case #1232');

-- 2019-08-06 Remove person_id from clinicalvisit_history table
ALTER TABLE clinicalvisit_history DROP COLUMN person_id;

INSERT INTO schema_version (version_number, comment) VALUES (161, 'Remove person_id from clinicalvisit_history table');

-- 2019-08-15 Add completeness value to case #1253
ALTER TABLE cases ADD COLUMN completeness real;
ALTER TABLE cases_history ADD COLUMN completeness real;

INSERT INTO schema_version (version_number, comment) VALUES (162, 'Add completeness value to case #1253');

-- 2019-09-08 Add deleted flag to core entities #1268
ALTER TABLE cases ADD COLUMN deleted boolean;
ALTER TABLE cases_history ADD COLUMN deleted boolean;
UPDATE cases SET deleted = false;
ALTER TABLE contact ADD COLUMN deleted boolean;
ALTER TABLE contact_history ADD COLUMN deleted boolean;
UPDATE contact SET deleted = false;
ALTER TABLE samples ADD COLUMN deleted boolean;
ALTER TABLE samples_history ADD COLUMN deleted boolean;
UPDATE samples SET deleted = false;
ALTER TABLE pathogentest ADD COLUMN deleted boolean;
ALTER TABLE pathogentest_history ADD COLUMN deleted boolean;
UPDATE pathogentest SET deleted = false;
ALTER TABLE events ADD COLUMN deleted boolean;
ALTER TABLE events_history ADD COLUMN deleted boolean;
UPDATE events SET deleted = false;

INSERT INTO schema_version (version_number, comment) VALUES (163, 'Add deleted flag to core entities #1268');

-- 2019-09-10 Add ExportConfiguration entity #1276
CREATE TABLE exportconfiguration(
	id bigint not null,
	uuid varchar(36) not null unique,
	changedate timestamp not null,
	creationdate timestamp not null,
	name varchar(512),
	user_id bigint,
	exporttype varchar(255),
	propertiesstring varchar,
	sys_period tstzrange not null,
	primary key(id)
);

ALTER TABLE exportconfiguration OWNER TO sormas_user;

CREATE TABLE exportconfiguration_history (LIKE exportconfiguration);
CREATE TRIGGER versioning_trigger
BEFORE INSERT OR UPDATE OR DELETE ON exportconfiguration
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'exportconfiguration_history', true);
ALTER TABLE exportconfiguration_history OWNER TO sormas_user;

ALTER TABLE exportconfiguration ADD CONSTRAINT fk_exportconfiguration_user_id FOREIGN KEY (user_id) REFERENCES users(id);

INSERT INTO schema_version (version_number, comment) VALUES (164, 'Add ExportConfiguration entity #1276');

-- 2019-09-11 Add PopulationData entity #1084
CREATE TABLE populationdata(
	id bigint not null,
	uuid varchar(36) not null unique,
	changedate timestamp not null,
	creationdate timestamp not null,
	region_id bigint,
	district_id bigint,
	sex varchar(255),
	agegroup varchar(255),
	population integer,
	collectiondate timestamp,
	primary key(id)
);

ALTER TABLE populationdata OWNER TO sormas_user;
ALTER TABLE populationdata ADD CONSTRAINT fk_populationdata_region_id FOREIGN KEY (region_id) REFERENCES region(id);
ALTER TABLE populationdata ADD CONSTRAINT fk_populationdata_district_id FOREIGN KEY (district_id) REFERENCES district(id);

ALTER TABLE region DROP COLUMN population;
ALTER TABLE district DROP COLUMN population;

INSERT INTO schema_version (version_number, comment) VALUES (165, 'Add PopulationData entity #1084');

-- 2019-09-26 Add pathogenTestResultChangeDate to sample #1302
ALTER TABLE samples ADD COLUMN pathogentestresultchangedate timestamp;
ALTER TABLE samples_history ADD COLUMN pathogentestresultchangedate timestamp;

INSERT INTO schema_version (version_number, comment) VALUES (166, 'Add pathogenTestResultChangeDate to sample #1302');

-- 2019-11-04 Fill samples with accurate pathogenTestResultChangeDate #1349
UPDATE samples SET pathogentestresultchangedate = (SELECT testdatetime FROM pathogentest WHERE pathogentest.sample_id = samples.id ORDER BY pathogentest.testdatetime DESC LIMIT 1);

INSERT INTO schema_version (version_number, comment) VALUES (167, 'Fill samples with accurate pathogenTestResultChangeDate #1349');

-- 2016-10-18; #982 additional fields to meningitis
ALTER TABLE pathogentest ADD COLUMN serotype varchar(255);
ALTER TABLE pathogentest_history ADD COLUMN serotype varchar(255);

ALTER TABLE pathogentest ADD COLUMN cqvalue real;
ALTER TABLE pathogentest_history ADD COLUMN cqvalue real;

INSERT INTO schema_version (version_number, comment) VALUES (168, 'Additional fields to meningitis #982');

-- 2019-11-06 Bed-side lab testing #1109
ALTER TABLE samples ADD COLUMN samplepurpose varchar(255) DEFAULT 'EXTERNAL' NOT NULL;
ALTER TABLE samples_history ADD COLUMN samplepurpose varchar(255) DEFAULT 'EXTERNAL' NOT NULL;
ALTER TABLE samples ALTER COLUMN lab_id DROP NOT NULL;
ALTER TABLE samples_history ALTER COLUMN lab_id DROP NOT NULL;
ALTER TABLE pathogentest ALTER COLUMN lab_id DROP NOT NULL;
ALTER TABLE pathogentest_history ALTER COLUMN lab_id DROP NOT NULL;
ALTER TABLE pathogentest ALTER COLUMN labuser_id DROP NOT NULL;
ALTER TABLE pathogentest_history ALTER COLUMN labuser_id DROP NOT NULL;

INSERT INTO schema_version (version_number, comment) VALUES (169, ' Bed-side lab testing #1109');

-- 2019-11-14 New disease: Human Rabies #834

ALTER TABLE cases ADD COLUMN vaccine varchar(512);
ALTER TABLE cases_history ADD COLUMN vaccine varchar(512);

ALTER TABLE epidata ADD COLUMN kindofexposurebite varchar(255);
ALTER TABLE epidata_history ADD COLUMN kindofexposurebite varchar(255);
ALTER TABLE epidata ADD COLUMN kindofexposuretouch varchar(255);
ALTER TABLE epidata_history ADD COLUMN kindofexposuretouch varchar(255);
ALTER TABLE epidata ADD COLUMN kindofexposurescratch varchar(255);
ALTER TABLE epidata_history ADD COLUMN kindofexposurescratch varchar(255);
ALTER TABLE epidata ADD COLUMN kindofexposurelick varchar(255);
ALTER TABLE epidata_history ADD COLUMN kindofexposurelick varchar(255);
ALTER TABLE epidata ADD COLUMN kindofexposureother varchar(255);
ALTER TABLE epidata_history ADD COLUMN kindofexposureother varchar(255);
ALTER TABLE epidata ADD COLUMN kindofexposuredetails varchar(512);
ALTER TABLE epidata_history ADD COLUMN kindofexposuredetails varchar(512);

ALTER TABLE epidata ADD COLUMN animalvaccinationstatus varchar(255);
ALTER TABLE epidata_history ADD COLUMN animalvaccinationstatus varchar(255);

ALTER TABLE symptoms ADD COLUMN hydrophobia varchar(255);
ALTER TABLE symptoms_history ADD COLUMN hydrophobia varchar(255);
ALTER TABLE symptoms ADD COLUMN opisthotonus varchar(255);
ALTER TABLE symptoms_history ADD COLUMN opisthotonus varchar(255);
ALTER TABLE symptoms ADD COLUMN anxietystates varchar(255);
ALTER TABLE symptoms_history ADD COLUMN anxietystates varchar(255);
ALTER TABLE symptoms ADD COLUMN delirium varchar(255);
ALTER TABLE symptoms_history ADD COLUMN delirium varchar(255);
ALTER TABLE symptoms ADD COLUMN uproariousness varchar(255);
ALTER TABLE symptoms_history ADD COLUMN uproariousness varchar(255);
ALTER TABLE symptoms ADD COLUMN paresthesiaaroundwound varchar(255);
ALTER TABLE symptoms_history ADD COLUMN paresthesiaaroundwound varchar(255);
ALTER TABLE symptoms ADD COLUMN excesssalivation varchar(255);
ALTER TABLE symptoms_history ADD COLUMN excesssalivation varchar(255);
ALTER TABLE symptoms ADD COLUMN insomnia varchar(255);
ALTER TABLE symptoms_history ADD COLUMN insomnia varchar(255);
ALTER TABLE symptoms ADD COLUMN paralysis varchar(255);
ALTER TABLE symptoms_history ADD COLUMN paralysis varchar(255);
ALTER TABLE symptoms ADD COLUMN excitation varchar(255);
ALTER TABLE symptoms_history ADD COLUMN excitation varchar(255);
ALTER TABLE symptoms ADD COLUMN dysphagia varchar(255);
ALTER TABLE symptoms_history ADD COLUMN dysphagia varchar(255);
ALTER TABLE symptoms ADD COLUMN aerophobia varchar(255);
ALTER TABLE symptoms_history ADD COLUMN aerophobia varchar(255);
ALTER TABLE symptoms ADD COLUMN hyperactivity varchar(255);
ALTER TABLE symptoms_history ADD COLUMN hyperactivity varchar(255);
ALTER TABLE symptoms ADD COLUMN paresis varchar(255);
ALTER TABLE symptoms_history ADD COLUMN paresis varchar(255);
ALTER TABLE symptoms ADD COLUMN agitation varchar(255);
ALTER TABLE symptoms_history ADD COLUMN agitation varchar(255);
ALTER TABLE symptoms ADD COLUMN ascendingflaccidparalysis varchar(255);
ALTER TABLE symptoms_history ADD COLUMN ascendingflaccidparalysis varchar(255);
ALTER TABLE symptoms ADD COLUMN erraticbehaviour varchar(255);
ALTER TABLE symptoms_history ADD COLUMN erraticbehaviour varchar(255);
ALTER TABLE symptoms ADD COLUMN coma varchar(255);
ALTER TABLE symptoms_history ADD COLUMN coma varchar(255);

ALTER TABLE cases ADD COLUMN rabiestype varchar(255);
ALTER TABLE cases_history ADD COLUMN rabiestype varchar(255);

ALTER TABLE epidata ADD COLUMN dogs varchar(255);
ALTER TABLE epidata_history ADD COLUMN dogs varchar(255);
ALTER TABLE epidata ADD COLUMN cats varchar(255);
ALTER TABLE epidata_history ADD COLUMN cats varchar(255);
ALTER TABLE epidata ADD COLUMN canidae varchar(255);
ALTER TABLE epidata_history ADD COLUMN canidae varchar(255);
ALTER TABLE epidata ADD COLUMN rabbits varchar(255);
ALTER TABLE epidata_history ADD COLUMN rabbits varchar(255);

ALTER TABLE epidata ADD COLUMN prophylaxisstatus varchar(255);
ALTER TABLE epidata_history ADD COLUMN prophylaxisstatus varchar(255);
ALTER TABLE epidata ADD COLUMN dateofprophylaxis timestamp;
ALTER TABLE epidata_history ADD COLUMN dateofprophylaxis timestamp;

INSERT INTO schema_version (version_number, comment) VALUES (170, 'Add new disease, human rabies #834');

-- 2019-11-17 Add relationship details field to contact #1067
ALTER TABLE contact ADD COLUMN relationdescription varchar(512);

INSERT INTO schema_version (version_number, comment) VALUES (171, 'Add relationship description to contact #1067');

-- 2019-11-13 Add new disease, Anthrax #833
ALTER TABLE symptoms ADD COLUMN convulsion varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (172, 'Add new disease, Anthrax #833');

-- 2019-11-27 Add FeatureConfiguration entity #1346
CREATE TABLE featureconfiguration(
	id bigint not null,
	uuid varchar(36) not null unique,
	changedate timestamp not null,
	creationdate timestamp not null,
	featuretype varchar(255),
	region_id bigint,
	district_id bigint,
	disease varchar(255),
	enddate timestamp,
	sys_period tstzrange not null,
	primary key(id)
);

ALTER TABLE featureconfiguration OWNER TO sormas_user;
ALTER TABLE featureconfiguration ADD CONSTRAINT fk_featureconfiguration_region_id FOREIGN KEY (region_id) REFERENCES region(id);
ALTER TABLE featureconfiguration ADD CONSTRAINT fk_featureconfiguration_district_id FOREIGN KEY (district_id) REFERENCES district(id);

CREATE TABLE featureconfiguration_history (LIKE featureconfiguration);
CREATE TRIGGER versioning_trigger
BEFORE INSERT OR UPDATE OR DELETE ON featureconfiguration
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'featureconfiguration_history', true);
ALTER TABLE featureconfiguration_history OWNER TO sormas_user;

INSERT INTO schema_version (version_number, comment) VALUES (173, 'Add FeatureConfiguration entity #1346');

-- 2019-12-03 Add port health infos to cases that are missing one #1377
DO $$
DECLARE rec RECORD;
DECLARE new_porthealthinfo_id INTEGER;
BEGIN
FOR rec IN SELECT id FROM public.cases WHERE porthealthinfo_id IS NULL
LOOP
INSERT INTO porthealthinfo(id, uuid, creationdate, changedate) VALUES (nextval('entity_seq'), upper(substring(CAST(CAST(md5(CAST(random() AS text) || CAST(clock_timestamp() AS text)) AS uuid) AS text), 3, 29)), now(), now()) RETURNING id INTO new_porthealthinfo_id;
UPDATE cases SET porthealthinfo_id = new_porthealthinfo_id WHERE id = rec.id;
END LOOP;
END;
$$ LANGUAGE plpgsql;

INSERT INTO schema_version (version_number, comment) VALUES (174, 'Add port health infos to cases that are missing one #1377');

-- 2019-12-04 Aggregate reports #1277
CREATE TABLE aggregatereport(
	id bigint not null,
	uuid varchar(36) not null unique,
	changedate timestamp not null,
	creationdate timestamp not null,
	reportinguser_id bigint,
	region_id bigint,
	district_id bigint,
	healthfacility_id bigint,
	pointofentry_id bigint,
	disease varchar(255),
	year integer,
	epiweek integer,
	newcases integer,
	labconfirmations integer,
	deaths integer,
	sys_period tstzrange not null,
	primary key(id)
);

ALTER TABLE aggregatereport OWNER TO sormas_user;
ALTER TABLE aggregatereport ADD CONSTRAINT fk_aggregatereport_region_id FOREIGN KEY (region_id) REFERENCES region(id);
ALTER TABLE aggregatereport ADD CONSTRAINT fk_aggregatereport_district_id FOREIGN KEY (district_id) REFERENCES district(id);
ALTER TABLE aggregatereport ADD CONSTRAINT fk_aggregatereport_healthfacility_id FOREIGN KEY (healthfacility_id) REFERENCES facility(id);
ALTER TABLE aggregatereport ADD CONSTRAINT fk_aggregatereport_pointofentry_id FOREIGN KEY (pointofentry_id) REFERENCES pointofentry(id);
ALTER TABLE aggregatereport ADD CONSTRAINT fk_aggregatereport_reportinguser_id FOREIGN KEY (reportinguser_id) REFERENCES users(id);

CREATE TABLE aggregatereport_history (LIKE aggregatereport);
CREATE TRIGGER versioning_trigger
BEFORE INSERT OR UPDATE OR DELETE ON aggregatereport
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'aggregatereport_history', true);
ALTER TABLE aggregatereport_history OWNER TO sormas_user;

INSERT INTO schema_version (version_number, comment) VALUES (175, 'Aggregate reports #1277');

-- 2019-12-05 Add caseBased column to diseaseconfiguration #1277
ALTER TABLE diseaseconfiguration ADD COLUMN casebased boolean;
ALTER TABLE diseaseconfiguration_history ADD COLUMN casebased boolean;

INSERT INTO schema_version (version_number, comment) VALUES (176, 'Add caseBased column to diseaseconfiguration #1277');

-- 2020-01-08 Add archived to infrastructure data #1412
ALTER TABLE region ADD COLUMN archived boolean DEFAULT false;
ALTER TABLE district ADD COLUMN archived boolean DEFAULT false;
ALTER TABLE community ADD COLUMN archived boolean DEFAULT false;
ALTER TABLE facility ADD COLUMN archived boolean DEFAULT false;
ALTER TABLE pointofentry ADD COLUMN archived boolean DEFAULT false;

INSERT INTO schema_version (version_number, comment) VALUES (177, 'Add archived to infrastructure data #1412');

-- 2020-01-28 Add fields for Coronavirus #1476
ALTER TABLE symptoms ADD COLUMN fluidinlungcavityauscultation varchar(255);
ALTER TABLE symptoms ADD COLUMN fluidinlungcavityxray varchar(255);
ALTER TABLE symptoms ADD COLUMN abnormallungxrayfindings varchar(255);
ALTER TABLE symptoms ADD COLUMN conjunctivalinjection varchar(255);
ALTER TABLE symptoms ADD COLUMN acuterespiratorydistresssyndrome varchar(255);
ALTER TABLE symptoms ADD COLUMN pneumoniaclinicalorradiologic varchar(255);
ALTER TABLE symptoms_history ADD COLUMN fluidinlungcavityauscultation varchar(255);
ALTER TABLE symptoms_history ADD COLUMN fluidinlungcavityxray varchar(255);
ALTER TABLE symptoms_history ADD COLUMN abnormallungxrayfindings varchar(255);
ALTER TABLE symptoms_history ADD COLUMN conjunctivalinjection varchar(255);
ALTER TABLE symptoms_history ADD COLUMN acuterespiratorydistresssyndrome varchar(255);
ALTER TABLE symptoms_history ADD COLUMN pneumoniaclinicalorradiologic varchar(255);

ALTER TABLE epidata ADD COLUMN visitedhealthfacility varchar(255);
ALTER TABLE epidata ADD COLUMN contactwithsourcerespiratorycase varchar(255);
ALTER TABLE epidata ADD COLUMN visitedanimalmarket varchar(255);
ALTER TABLE epidata ADD COLUMN camels varchar(255);
ALTER TABLE epidata ADD COLUMN snakes varchar(255);
ALTER TABLE epidata_history ADD COLUMN visitedhealthfacility varchar(255);
ALTER TABLE epidata_history ADD COLUMN contactwithsourcerespiratorycase varchar(255);
ALTER TABLE epidata_history ADD COLUMN visitedanimalmarket varchar(255);
ALTER TABLE epidata_history ADD COLUMN camels varchar(255);
ALTER TABLE epidata_history ADD COLUMN snakes varchar(255);

ALTER TABLE healthconditions ADD COLUMN immunodeficiencyotherthanhiv varchar(255);
ALTER TABLE healthconditions ADD COLUMN cardiovasculardiseaseincludinghypertension varchar(255);
ALTER TABLE healthconditions_history ADD COLUMN immunodeficiencyotherthanhiv varchar(255);
ALTER TABLE healthconditions_history ADD COLUMN cardiovasculardiseaseincludinghypertension varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (178, 'Add fields for Coronavirus #1476');

-- 2020-01-28 Set missing point of entry type for default entries #1484
UPDATE pointofentry SET pointofentrytype='AIRPORT', changedate=now() WHERE uuid='SORMAS-CONSTID-OTHERS-AIRPORTX';
UPDATE pointofentry SET pointofentrytype='SEAPORT', changedate=now() WHERE uuid='SORMAS-CONSTID-OTHERS-SEAPORTX';
UPDATE pointofentry SET pointofentrytype='GROUND_CROSSING', changedate=now() WHERE uuid='SORMAS-CONSTIG-OTHERS-GROUNDCR';
UPDATE pointofentry SET pointofentrytype='OTHER', changedate=now() WHERE uuid='SORMAS-CONSTID-OTHERS-OTHERPOE';

INSERT INTO schema_version (version_number, comment) VALUES (179, 'Set missing point of entry type for default entries #1484');

-- 2020-01-28 Rename "New influenca" to "New influenza" #1458
UPDATE cases SET disease = 'NEW_INFLUENZA' where disease = 'NEW_INFLUENCA';
UPDATE cases_history SET disease = 'NEW_INFLUENZA' where disease = 'NEW_INFLUENCA';
UPDATE events SET disease = 'NEW_INFLUENZA' where disease = 'NEW_INFLUENCA';
UPDATE events_history SET disease = 'NEW_INFLUENZA' where disease = 'NEW_INFLUENCA';
UPDATE outbreak SET disease = 'NEW_INFLUENZA' where disease = 'NEW_INFLUENCA';
UPDATE outbreak_history SET disease = 'NEW_INFLUENZA' where disease = 'NEW_INFLUENCA';
UPDATE person SET causeofdeathdisease = 'NEW_INFLUENZA' where causeofdeathdisease = 'NEW_INFLUENCA';
UPDATE person_history SET causeofdeathdisease = 'NEW_INFLUENZA' where causeofdeathdisease = 'NEW_INFLUENCA';
UPDATE visit SET disease = 'NEW_INFLUENZA' where disease = 'NEW_INFLUENCA';
UPDATE visit_history SET disease = 'NEW_INFLUENZA' where disease = 'NEW_INFLUENCA';
UPDATE weeklyreportentry SET disease = 'NEW_INFLUENZA' where disease = 'NEW_INFLUENCA';
UPDATE clinicalvisit SET disease = 'NEW_INFLUENZA' where disease = 'NEW_INFLUENCA';
UPDATE clinicalvisit_history SET disease = 'NEW_INFLUENZA' where disease = 'NEW_INFLUENCA';
UPDATE diseaseconfiguration SET disease = 'NEW_INFLUENZA' where disease = 'NEW_INFLUENCA';
UPDATE diseaseconfiguration_history SET disease = 'NEW_INFLUENZA' where disease = 'NEW_INFLUENCA';
UPDATE featureconfiguration SET disease = 'NEW_INFLUENZA' where disease = 'NEW_INFLUENCA';
UPDATE featureconfiguration_history SET disease = 'NEW_INFLUENZA' where disease = 'NEW_INFLUENCA';
UPDATE pathogentest SET testeddisease = 'NEW_INFLUENZA' where testeddisease = 'NEW_INFLUENCA';
UPDATE pathogentest_history SET testeddisease = 'NEW_INFLUENZA' where testeddisease = 'NEW_INFLUENCA';
UPDATE users SET limiteddisease = 'NEW_INFLUENZA' where limiteddisease = 'NEW_INFLUENCA';
UPDATE users_history SET limiteddisease = 'NEW_INFLUENZA' where limiteddisease = 'NEW_INFLUENCA';

INSERT INTO schema_version (version_number, comment) VALUES (180, 'Rename "New influenca" to "New influenza" #1458');

-- 2020-02-19 Add language to user #1093
ALTER TABLE users ADD COLUMN language varchar(255);
ALTER TABLE users_history ADD COLUMN language varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (181, 'Add language to user #1093');

-- 2020-03-09 Add additional details to case #1564
ALTER TABLE cases ADD COLUMN additionaldetails varchar(512);
ALTER TABLE cases_history ADD COLUMN additionaldetails varchar(512);

INSERT INTO schema_version (version_number, comment) VALUES (182, 'Add additional details to case #1564');

-- 2020-03-10 Add postal code to location #1553
ALTER TABLE location ADD COLUMN postalcode varchar(255);
ALTER TABLE location_history ADD COLUMN postalcode varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (183, 'Add postal code to location #1553');

-- 2020-03-10 Add enabled field to feature configuration #1559
ALTER TABLE featureconfiguration ADD COLUMN enabled boolean;
ALTER TABLE featureconfiguration_history ADD COLUMN enabled boolean;
UPDATE featureconfiguration SET enabled = true;

INSERT INTO schema_version (version_number, comment) VALUES (184, 'Add enabled field to feature configuration #1559');

-- 2020-03-10 Add external ID text field to case and contact #1571
ALTER TABLE cases ADD COLUMN externalid varchar(255);
ALTER TABLE cases_history ADD COLUMN externalid varchar(255);
ALTER TABLE contact ADD COLUMN externalid varchar(255);
ALTER TABLE contact_history ADD COLUMN externalid varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (185, 'Add external ID text field to case and contact #1571');

-- 2020-03-11 Add region and district to contact #1561
ALTER TABLE contact ADD COLUMN region_id bigint;
ALTER TABLE contact ADD COLUMN district_id bigint;
ALTER TABLE contact_history ADD COLUMN region_id bigint;
ALTER TABLE contact_history ADD COLUMN district_id bigint;

ALTER TABLE contact ADD CONSTRAINT fk_contact_region_id FOREIGN KEY (region_id) REFERENCES region (id);
ALTER TABLE contact ADD CONSTRAINT fk_contact_district_id FOREIGN KEY (district_id) REFERENCES district (id);

INSERT INTO schema_version (version_number, comment) VALUES (186, 'Add region and district to contact #1561');

-- 2020-03-13 Add sharedToCountry to case #1562
ALTER TABLE cases ADD COLUMN sharedtocountry boolean;
ALTER TABLE cases_history ADD COLUMN sharedtocountry boolean;
UPDATE cases SET sharedtocountry = false;

INSERT INTO schema_version (version_number, comment) VALUES (187, 'Add sharedToCountry to case #1562');

-- 2020-03-17 Add high priority status to contact #1595
ALTER TABLE contact ADD COLUMN highpriority boolean;
UPDATE contact SET highpriority = false;
ALTER TABLE contact ADD COLUMN immunosuppressiveTherapyBasicDisease varchar(255);
ALTER TABLE contact ADD COLUMN immunosuppressiveTherapyBasicDiseaseDetails varchar(512);
ALTER TABLE contact ADD COLUMN careForPeopleOver60 varchar(255);
ALTER TABLE contact_history ADD COLUMN highpriority boolean;
ALTER TABLE contact_history ADD COLUMN immunosuppressiveTherapyBasicDisease varchar(255);
ALTER TABLE contact_history ADD COLUMN immunosuppressiveTherapyBasicDiseaseDetails varchar(512);
ALTER TABLE contact_history ADD COLUMN careForPeopleOver60 varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (188, 'Add high priority status to contact #1595');

-- 2020-03-17 Add general practitioner details to person #1600
ALTER TABLE person ADD COLUMN generalpractitionerdetails varchar(512);
ALTER TABLE person_history ADD COLUMN generalpractitionerdetails varchar(512);

INSERT INTO schema_version (version_number, comment) VALUES (189, 'Add general practitioner details to person #1600');

-- 2020-03-18 Add external ID field to region, district, community, facility and point of entry #1604
ALTER TABLE region ADD COLUMN externalid varchar(255);
ALTER TABLE district ADD COLUMN externalid varchar(255);
ALTER TABLE community ADD COLUMN externalid varchar(255);
ALTER TABLE facility ADD COLUMN externalid varchar(255);
ALTER TABLE pointofentry ADD COLUMN externalid varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (190, 'Add external ID field to region, district, community, facility and point of entry #1604');

-- 2020-03-19 Add quarantine information to contact #1608
ALTER TABLE contact ADD COLUMN quarantine varchar(255);
ALTER TABLE contact ADD COLUMN quarantinefrom timestamp;
ALTER TABLE contact ADD COLUMN quarantineto timestamp;
ALTER TABLE contact_history ADD COLUMN quarantine varchar(255);
ALTER TABLE contact_history ADD COLUMN quarantinefrom timestamp;
ALTER TABLE contact_history ADD COLUMN quarantineto timestamp;

INSERT INTO schema_version (version_number, comment) VALUES (191, 'Add quarantine information to contact #1608');

-- 2020-03-24 Add disease to contact #1643
ALTER TABLE contact ADD COLUMN disease varchar(255);
ALTER TABLE contact ADD COLUMN diseasedetails varchar(512);
ALTER TABLE contact_history ADD COLUMN disease varchar(255);
ALTER TABLE contact_history ADD COLUMN diseasedetails varchar(512);

UPDATE contact SET disease = cases.disease FROM cases WHERE contact.caze_id = cases.id;
UPDATE contact SET diseasedetails = cases.diseasedetails FROM cases WHERE contact.caze_id = cases.id;

INSERT INTO schema_version (version_number, comment) VALUES (192, 'Add disease to contact #1643');

-- 2020-03-25 Allow creation of contacts without a case #1599
ALTER TABLE contact ALTER COLUMN caze_id DROP NOT NULL;
ALTER TABLE contact_history ALTER COLUMN caze_id DROP NOT NULL;

ALTER TABLE contact ADD COLUMN caseidexternalsystem varchar(255);
ALTER TABLE contact ADD COLUMN caseoreventinformation varchar(512);
ALTER TABLE contact_history ADD COLUMN caseidexternalsystem varchar(255);
ALTER TABLE contact_history ADD COLUMN caseoreventinformation varchar(512);

INSERT INTO schema_version (version_number, comment) VALUES (193, 'Allow creation of contacts without a case #1599');

-- 2020-03-30 Add email address, passport number and national health id to person #1639 & #1681
ALTER TABLE person ADD COLUMN emailaddress varchar(255);
ALTER TABLE person ADD COLUMN passportnumber varchar(255);
ALTER TABLE person ADD COLUMN nationalhealthid varchar(255);
ALTER TABLE person_history ADD COLUMN emailaddress varchar(255);
ALTER TABLE person_history ADD COLUMN passportnumber varchar(255);
ALTER TABLE person_history ADD COLUMN nationalhealthid varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (194, 'Add email address, passport number and national health id to person #1639 & #1681');

-- 2020-03-30 Add quarantine information to case #1675
ALTER TABLE cases ADD COLUMN quarantine varchar(255);
ALTER TABLE cases ADD COLUMN quarantinefrom timestamp;
ALTER TABLE cases ADD COLUMN quarantineto timestamp;
ALTER TABLE cases_history ADD COLUMN quarantine varchar(255);
ALTER TABLE cases_history ADD COLUMN quarantinefrom timestamp;
ALTER TABLE cases_history ADD COLUMN quarantineto timestamp;

INSERT INTO schema_version (version_number, comment) VALUES (195, 'Add quarantine information to case #1675');

-- 2020-03-27 Add contact category and type of contact comment #1635
ALTER TABLE contact ADD COLUMN contactcategory varchar(255);
ALTER TABLE contact ADD COLUMN contactproximitydetails varchar(512);
ALTER TABLE contact_history ADD COLUMN contactcategory varchar(255);
ALTER TABLE contact_history ADD COLUMN contactproximitydetails varchar(512);
-- Compromise solution for international systems (non-synchronized data from mobile devices are neglected):
UPDATE contact SET contactproximitydetails = 'Airplane' WHERE contactproximity = 'AIRPLANE';

INSERT INTO schema_version (version_number, comment) VALUES (196, 'Add contact category and type of contact comment #1635');

-- 2020-03-31 Make follow-up until editable #1680
ALTER TABLE contact ADD COLUMN overwritefollowupuntil boolean;
ALTER TABLE contact_history ADD COLUMN overwritefollowupuntil boolean;

UPDATE contact SET overwritefollowupuntil = false;

INSERT INTO schema_version (version_number, comment) VALUES (197, 'Make follow-up until editable #1680');

-- 2020-04-15 Reworking of quarantine #1762

-- 2020-04-29 #1891 keep the old quarantineto database colum
-- UPDATE contact SET 	followupuntil = quarantineto WHERE quarantineto > followupuntil;
-- ALTER TABLE contact DROP COLUMN quarantineto;
-- ALTER TABLE contact_history DROP COLUMN quarantineto;

ALTER TABLE contact ADD COLUMN quarantineordermeans varchar(255);
ALTER TABLE contact_history ADD COLUMN quarantineordermeans varchar(255);

ALTER TABLE contact ADD COLUMN quarantinehelpneeded varchar(512);
ALTER TABLE contact_history ADD COLUMN quarantinehelpneeded varchar(512);

INSERT INTO schema_version (version_number, comment) VALUES (198, 'Reworking of quarantine #1762');

-- 2020-04-20 Add fields for intensive care unit to hospitalization #1830
ALTER TABLE hospitalization ADD COLUMN intensivecareunit varchar(255);
ALTER TABLE hospitalization_history ADD COLUMN intensivecareunit varchar(255);
ALTER TABLE hospitalization ADD COLUMN intensivecareunitstart timestamp;
ALTER TABLE hospitalization_history ADD COLUMN intensivecareunitstart timestamp;
ALTER TABLE hospitalization ADD COLUMN intensivecareunitend timestamp;
ALTER TABLE hospitalization_history ADD COLUMN intensivecareunitend timestamp;

UPDATE hospitalization SET 	intensivecareunit = 'YES' WHERE accommodation = 'ICU';

ALTER TABLE hospitalization DROP COLUMN accommodation;
ALTER TABLE hospitalization_history DROP COLUMN accommodation;

INSERT INTO schema_version (version_number, comment) VALUES (199, 'Add fields for intensive care unit to hospitalization #1830');

-- 2020-04-22 Remove export functions which are now maintained within java code  #1830
DROP FUNCTION export_database(text, text);
DROP FUNCTION export_database_join(text, text, text, text, text);

INSERT INTO schema_version (version_number, comment) VALUES (200, 'Remove export functions which are now maintained within java code #1830');

-- 2020-04-23 Re-introduce quarantine end date #1891
-- 2020-05-11 #1952: Was needed for #1891 to make sure existing SORMAS systems
-- pre and schema version 198 were correctly updated. No removed due to incompatibility with PostgreSQL 9.5
-- ALTER TABLE contact ADD COLUMN IF NOT EXISTS quarantineto timestamp;
-- ALTER TABLE contact_history ADD COLUMN IF NOT EXISTS quarantineto timestamp;

INSERT INTO schema_version (version_number, comment) VALUES (201, 'Re-introduce quarantine end date #1891');

-- 2020-04-24 Additional quarantine fields #1906
ALTER TABLE cases ADD COLUMN quarantinehelpneeded varchar(512);
ALTER TABLE cases ADD COLUMN quarantineorderedverbally boolean;
ALTER TABLE cases ADD COLUMN quarantineorderedofficialdocument boolean;
ALTER TABLE cases ADD COLUMN quarantineorderedverballydate timestamp;
ALTER TABLE cases ADD COLUMN quarantineorderedofficialdocumentdate timestamp;
ALTER TABLE cases ADD COLUMN quarantinehomepossible varchar(255);
ALTER TABLE cases ADD COLUMN quarantinehomepossiblecomment varchar(512);
ALTER TABLE cases ADD COLUMN quarantinehomesupplyensured varchar(255);
ALTER TABLE cases ADD COLUMN quarantinehomesupplyensuredcomment varchar(512);
ALTER TABLE cases_history ADD COLUMN quarantinehelpneeded varchar(512);
ALTER TABLE cases_history ADD COLUMN quarantineorderedverbally boolean;
ALTER TABLE cases_history ADD COLUMN quarantineorderedofficialdocument boolean;
ALTER TABLE cases_history ADD COLUMN quarantineorderedverballydate timestamp;
ALTER TABLE cases_history ADD COLUMN quarantineorderedofficialdocumentdate timestamp;
ALTER TABLE cases_history ADD COLUMN quarantinehomepossible varchar(255);
ALTER TABLE cases_history ADD COLUMN quarantinehomepossiblecomment varchar(512);
ALTER TABLE cases_history ADD COLUMN quarantinehomesupplyensured varchar(255);
ALTER TABLE cases_history ADD COLUMN quarantinehomesupplyensuredcomment varchar(512);

UPDATE cases SET quarantineorderedverbally = false;
UPDATE cases SET quarantineorderedofficialdocument = false;

ALTER TABLE contact ADD COLUMN quarantineorderedverbally boolean;
ALTER TABLE contact ADD COLUMN quarantineorderedofficialdocument boolean;
ALTER TABLE contact ADD COLUMN quarantineorderedverballydate timestamp;
ALTER TABLE contact ADD COLUMN quarantineorderedofficialdocumentdate timestamp;
ALTER TABLE contact ADD COLUMN quarantinehomepossible varchar(255);
ALTER TABLE contact ADD COLUMN quarantinehomepossiblecomment varchar(512);
ALTER TABLE contact ADD COLUMN quarantinehomesupplyensured varchar(255);
ALTER TABLE contact ADD COLUMN quarantinehomesupplyensuredcomment varchar(512);
ALTER TABLE contact_history ADD COLUMN quarantineorderedverbally boolean;
ALTER TABLE contact_history ADD COLUMN quarantineorderedofficialdocument boolean;
ALTER TABLE contact_history ADD COLUMN quarantineorderedverballydate timestamp;
ALTER TABLE contact_history ADD COLUMN quarantineorderedofficialdocumentdate timestamp;
ALTER TABLE contact_history ADD COLUMN quarantinehomepossible varchar(255);
ALTER TABLE contact_history ADD COLUMN quarantinehomepossiblecomment varchar(512);
ALTER TABLE contact_history ADD COLUMN quarantinehomesupplyensured varchar(255);
ALTER TABLE contact_history ADD COLUMN quarantinehomesupplyensuredcomment varchar(512);

UPDATE contact SET quarantineorderedverbally = CASE WHEN quarantineordermeans = 'VERBALLY' THEN true ELSE false END;
UPDATE contact SET quarantineorderedofficialdocument = CASE WHEN quarantineordermeans = 'OFFICIAL_DOCUMENT' THEN true ELSE false END;

ALTER TABLE contact DROP COLUMN quarantineordermeans;
ALTER TABLE contact_history DROP COLUMN quarantineordermeans;

INSERT INTO schema_version (version_number, comment) VALUES (202, 'Additional quarantine fields #1906');

-- 2020-04-24 Add type of reporting to cases #1833
ALTER TABLE cases ADD COLUMN reportingtype varchar(255);
ALTER TABLE cases_history ADD COLUMN reportingtype varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (203, 'Add type of reporting to cases #1833');

-- 2020-04-26 Add fieldSampleID to sample #1863
ALTER TABLE samples ADD COLUMN fieldsampleid varchar(512);
ALTER TABLE samples_history ADD COLUMN fieldsampleid varchar(512);

INSERT INTO schema_version (version_number, comment) VALUES (204, 'Add fieldSampleID to sample #1863');

-- 2020-04-29 Added symptoms loss of taste and loss of smell #1936
ALTER TABLE symptoms ADD COLUMN lossoftaste varchar(255);
ALTER TABLE symptoms ADD COLUMN lossofsmell varchar(255);
ALTER TABLE symptoms_history ADD COLUMN lossoftaste varchar(255);
ALTER TABLE symptoms_history ADD COLUMN lossofsmell varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (205, 'Added symptoms loss of taste and loss of smell #1936');

-- 2020-05-05 Add new symptoms and health conditions #1824
ALTER TABLE symptoms ADD COLUMN coughWithSputum varchar(255);
ALTER TABLE symptoms ADD COLUMN coughWithHeamoptysis varchar(255);
ALTER TABLE symptoms ADD COLUMN lymphadenopathy varchar(255);
ALTER TABLE symptoms ADD COLUMN wheezing varchar(255);
ALTER TABLE symptoms ADD COLUMN skinUlcers varchar(255);
ALTER TABLE symptoms ADD COLUMN inabilityToWalk varchar(255);
ALTER TABLE symptoms ADD COLUMN inDrawingOfChestWall varchar(255);
ALTER TABLE symptoms ADD COLUMN otherComplications varchar(255);
ALTER TABLE symptoms ADD COLUMN otherComplicationsText varchar(255);
ALTER TABLE symptoms_history ADD COLUMN coughWithSputum varchar(255);
ALTER TABLE symptoms_history ADD COLUMN coughWithHeamoptysis varchar(255);
ALTER TABLE symptoms_history ADD COLUMN lymphadenopathy varchar(255);
ALTER TABLE symptoms_history ADD COLUMN wheezing varchar(255);
ALTER TABLE symptoms_history ADD COLUMN skinUlcers varchar(255);
ALTER TABLE symptoms_history ADD COLUMN inabilityToWalk varchar(255);
ALTER TABLE symptoms_history ADD COLUMN inDrawingOfChestWall varchar(255);
ALTER TABLE symptoms_history ADD COLUMN otherComplications varchar(255);
ALTER TABLE symptoms_history ADD COLUMN otherComplicationsText varchar(255);

ALTER TABLE healthconditions ADD COLUMN obesity varchar(255);
ALTER TABLE healthconditions ADD COLUMN currentSmoker varchar(255);
ALTER TABLE healthconditions ADD COLUMN formerSmoker varchar(255);
ALTER TABLE healthconditions ADD COLUMN asthma varchar(255);
ALTER TABLE healthconditions ADD COLUMN sickleCellDisease varchar(255);
ALTER TABLE healthconditions_history ADD COLUMN obesity varchar(255);
ALTER TABLE healthconditions_history ADD COLUMN currentSmoker varchar(255);
ALTER TABLE healthconditions_history ADD COLUMN formerSmoker varchar(255);
ALTER TABLE healthconditions_history ADD COLUMN asthma varchar(255);
ALTER TABLE healthconditions_history ADD COLUMN sickleCellDisease varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (206, 'Add new symptoms and health conditions #1824');

-- 2020-05-05 Added table for contact-visit association #1329
CREATE TABLE contacts_visits(
	contact_id bigint NOT NULL,
	visit_id bigint NOT NULL,
	sys_period tstzrange NOT NULL
);

ALTER TABLE contacts_visits OWNER TO sormas_user;
ALTER TABLE ONLY contacts_visits ADD CONSTRAINT unq_contacts_visits_0 UNIQUE (contact_id, visit_id);
ALTER TABLE ONLY contacts_visits ADD CONSTRAINT fk_contacts_visits_contact_id FOREIGN KEY (contact_id) REFERENCES contact(id);
ALTER TABLE ONLY contacts_visits ADD CONSTRAINT fk_contacts_visits_visit_id FOREIGN KEY (visit_id) REFERENCES visit(id);

CREATE TABLE contacts_visits_history (LIKE contacts_visits);
CREATE TRIGGER versioning_trigger BEFORE INSERT OR UPDATE OR DELETE ON contacts_visits
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'contacts_visits_history', true);
ALTER TABLE contacts_visits_history OWNER TO sormas_user;

WITH ids AS
(SELECT c.id AS contact_id, v.id AS visit_id FROM contact c, visit v WHERE c.person_id = v.person_id AND c.disease = v.disease AND
CASE
WHEN c.lastcontactdate IS NOT NULL THEN v.visitdatetime >= (c.lastcontactdate - interval '30' day)
ELSE v.visitdatetime >= (c.reportdatetime - interval '30' day)
END
AND
CASE
WHEN c.followupuntil IS NOT NULL THEN v.visitdatetime <= (c.followupuntil + interval '30' day)
WHEN c.lastcontactdate IS NOT NULL THEN v.visitdatetime <= (c.lastcontactdate + interval '30' day)
ELSE v.visitdatetime <= (c.reportdatetime + interval '30' day)
END)
INSERT INTO contacts_visits (contact_id, visit_id) SELECT contact_id, visit_id FROM ids;

INSERT INTO schema_version (version_number, comment) VALUES (207, 'Added table for contact-visit association #1329');

-- 2020-05-11 Add additionalDetails to contact #1933
ALTER TABLE contact ADD COLUMN additionaldetails varchar(512);
ALTER TABLE contact_history ADD COLUMN additionaldetails varchar(512);

INSERT INTO schema_version (version_number, comment) VALUES (208, 'Add additionalDetails to contact #1933');

-- 2020-05-18 Add Trimester and Postpartum selection to case #1981
ALTER TABLE cases ADD COLUMN postpartum varchar(255);
ALTER TABLE cases ADD COLUMN trimester varchar(255);
ALTER TABLE cases_history ADD COLUMN postpartum varchar(255);
ALTER TABLE cases_history ADD COLUMN trimester varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (209, 'Add Trimester and Postpartum selection to case #1981');

-- 2020-05-20 Adjust COVID-19 symptoms and health conditions for Germany #2097
ALTER TABLE symptoms ADD COLUMN respiratorydiseaseventilation varchar(255);
ALTER TABLE symptoms ADD COLUMN generalsignsofdisease varchar(255);
ALTER TABLE symptoms ADD COLUMN fastheartrate varchar(255);
ALTER TABLE symptoms ADD COLUMN oxygensaturationlower94 varchar(255);
ALTER TABLE symptoms_history ADD COLUMN respiratorydiseaseventilation varchar(255);
ALTER TABLE symptoms_history ADD COLUMN generalsignsofdisease varchar(255);
ALTER TABLE symptoms_history ADD COLUMN fastheartrate varchar(255);
ALTER TABLE symptoms_history ADD COLUMN oxygensaturationlower94 varchar(255);

ALTER TABLE healthconditions ADD COLUMN immunodeficiencyincludinghiv varchar(255);
ALTER TABLE healthconditions_history ADD COLUMN immunodeficiencyincludinghiv varchar(255);

UPDATE healthconditions SET immunodeficiencyincludinghiv = 'YES' WHERE hiv = 'YES' OR immunodeficiencyotherthanhiv = 'YES';
UPDATE healthconditions SET immunodeficiencyincludinghiv = 'NO' WHERE hiv = 'NO' AND immunodeficiencyotherthanhiv = 'NO';
UPDATE healthconditions SET immunodeficiencyincludinghiv = 'UNKNOWN' WHERE hiv = 'UNKNOWN ' AND immunodeficiencyotherthanhiv = 'UNKNOWN';

INSERT INTO schema_version (version_number, comment) VALUES (210, 'Adjust COVID-19 symptoms and health conditions for Germany #2097');

-- 2020-05-07 Add samples to contacts #1753
ALTER TABLE samples ADD COLUMN associatedcontact_id bigint;
ALTER TABLE samples ADD CONSTRAINT fk_samples_associatedcontact_id FOREIGN KEY (associatedcontact_id) REFERENCES contact (id);
ALTER TABLE samples ALTER COLUMN associatedcase_id DROP NOT NULL;
ALTER TABLE samples_history ADD COLUMN associatedcontact_id bigint;
ALTER TABLE samples_history ALTER COLUMN associatedcase_id DROP NOT NULL;

INSERT INTO schema_version (version_number, comment) VALUES (211, 'Add samples to contacts #1753');

-- 2020-05-28 Rename misspelled enum values #2094
UPDATE contact SET contactproximity = 'MEDICAL_UNSAFE' WHERE contactproximity = 'MEDICAL_UNSAVE';
UPDATE contact SET contactproximity = 'MEDICAL_SAFE' WHERE contactproximity = 'MEDICAL_SAVE';

INSERT INTO schema_version (version_number, comment) VALUES (212, 'Rename misspelled enum values #2094');

-- 2020-05-25 Add campaigns #1984
CREATE TABLE campaigns(
	id bigint not null,
	uuid varchar(36) not null unique,
	changedate timestamp not null,
	creationdate timestamp not null,
	name varchar(255),
	description varchar(512),
	startdate timestamp,
	enddate timestamp,
	creatinguser_id bigint,
	deleted boolean DEFAULT false,
	archived boolean DEFAULT false,
	sys_period tstzrange not null,
	primary key(id)
);

ALTER TABLE campaigns OWNER TO sormas_user;
ALTER TABLE campaigns ADD CONSTRAINT fk_campaigns_creatinguser_id FOREIGN KEY (creatinguser_id) REFERENCES users(id);
CREATE TABLE campaigns_history (LIKE campaigns);
CREATE TRIGGER versioning_trigger BEFORE INSERT OR UPDATE OR DELETE ON campaigns
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'campaigns_history', true);
ALTER TABLE campaigns_history OWNER TO sormas_user;

INSERT INTO schema_version (version_number, comment) VALUES (213, 'Add campaigns #1984');

-- 2020-06-04 Change text field lengths #580´
ALTER TABLE campaigns ALTER COLUMN description TYPE varchar(4096);
ALTER TABLE campaigns_history ALTER COLUMN description TYPE varchar(4096);

ALTER TABLE cases ALTER COLUMN description TYPE varchar(4096);
ALTER TABLE cases ALTER COLUMN additionalDetails TYPE varchar(4096);
ALTER TABLE cases_history ALTER COLUMN description TYPE varchar(4096);
ALTER TABLE cases_history ALTER COLUMN additionalDetails TYPE varchar(4096);

ALTER TABLE porthealthinfo ALTER COLUMN details TYPE varchar(4096);
ALTER TABLE porthealthinfo_history ALTER COLUMN details TYPE varchar(4096);

ALTER TABLE healthconditions ALTER COLUMN otherconditions TYPE varchar(4096);
ALTER TABLE healthconditions_history ALTER COLUMN otherconditions TYPE varchar(4096);

ALTER TABLE contact ALTER COLUMN followUpComment TYPE varchar(4096);
ALTER TABLE contact ALTER COLUMN caseoreventinformation TYPE varchar(4096);
ALTER TABLE contact ALTER COLUMN additionaldetails TYPE varchar(4096);
ALTER TABLE contact ALTER COLUMN description TYPE varchar(4096);
ALTER TABLE contact_history ADD COLUMN followUpComment varchar(4096);
ALTER TABLE contact_history ALTER COLUMN caseoreventinformation TYPE varchar(4096);
ALTER TABLE contact_history ALTER COLUMN additionaldetails TYPE varchar(4096);
ALTER TABLE contact_history ALTER COLUMN description TYPE varchar(4096);

ALTER TABLE epidatagathering ALTER COLUMN description TYPE varchar(4096);
ALTER TABLE epidatagathering_history ALTER COLUMN description TYPE varchar(4096);

ALTER TABLE events ALTER COLUMN eventdesc TYPE varchar(4096);
ALTER TABLE events_history ALTER COLUMN eventdesc TYPE varchar(4096);

ALTER TABLE previoushospitalization ALTER COLUMN description TYPE varchar(4096);
ALTER TABLE previoushospitalization_history ALTER COLUMN description TYPE varchar(4096);

ALTER TABLE location ALTER COLUMN address TYPE varchar(4096);
ALTER TABLE location_history ALTER COLUMN address TYPE varchar(4096);

ALTER TABLE additionaltest ALTER COLUMN othertestresults TYPE varchar(4096);
ALTER TABLE additionaltest_history ALTER COLUMN othertestresults TYPE varchar(4096);

ALTER TABLE pathogentest ALTER COLUMN testresulttext TYPE varchar(4096);
ALTER TABLE pathogentest_history ALTER COLUMN testresulttext TYPE varchar(4096);

ALTER TABLE samples ALTER COLUMN comment TYPE varchar(4096);
ALTER TABLE samples_history ALTER COLUMN comment TYPE varchar(4096);

ALTER TABLE task ALTER COLUMN creatorcomment TYPE varchar(4096);
ALTER TABLE task ALTER COLUMN assigneereply TYPE varchar(4096);
ALTER TABLE task_history ALTER COLUMN creatorcomment TYPE varchar(4096);
ALTER TABLE task_history ALTER COLUMN assigneereply TYPE varchar(4096);

ALTER TABLE prescription ALTER COLUMN additionalnotes TYPE varchar(4096);
ALTER TABLE prescription_history ALTER COLUMN additionalnotes TYPE varchar(4096);

ALTER TABLE treatment ALTER COLUMN additionalnotes TYPE varchar(4096);
ALTER TABLE treatment_history ALTER COLUMN additionalnotes TYPE varchar(4096);

ALTER TABLE cases ALTER COLUMN diseasedetails TYPE varchar(512);
ALTER TABLE cases ALTER COLUMN classificationcomment TYPE varchar(512);
ALTER TABLE cases ALTER COLUMN healthfacilitydetails TYPE varchar(512);
ALTER TABLE cases ALTER COLUMN cliniciandetails TYPE varchar(512);
ALTER TABLE cases ALTER COLUMN clinicianphone TYPE varchar(512);
ALTER TABLE cases ALTER COLUMN clinicianemail TYPE varchar(512);
ALTER TABLE cases ALTER COLUMN vaccinationdoses TYPE varchar(512);
ALTER TABLE cases ALTER COLUMN vaccine TYPE varchar(512);
ALTER TABLE cases ALTER COLUMN epidnumber TYPE varchar(512);
ALTER TABLE cases ALTER COLUMN sequelaedetails TYPE varchar(512);
ALTER TABLE cases ALTER COLUMN notifyingclinicdetails TYPE varchar(512);
ALTER TABLE cases ALTER COLUMN pointofentrydetails TYPE varchar(512);
ALTER TABLE cases ALTER COLUMN externalid TYPE varchar(512);
ALTER TABLE cases ALTER COLUMN quarantinehelpneeded TYPE varchar(512);
ALTER TABLE cases ALTER COLUMN quarantinehomepossiblecomment TYPE varchar(512);
ALTER TABLE cases ALTER COLUMN quarantinehomesupplyensuredcomment TYPE varchar(512);
ALTER TABLE cases_history ADD COLUMN diseasedetails varchar(512);
ALTER TABLE cases_history ADD COLUMN classificationcomment varchar(512);
ALTER TABLE cases_history ADD COLUMN healthfacilitydetails varchar(512);
ALTER TABLE cases_history ALTER COLUMN cliniciandetails TYPE varchar(512);
ALTER TABLE cases_history ALTER COLUMN clinicianphone TYPE varchar(512);
ALTER TABLE cases_history ALTER COLUMN clinicianemail TYPE varchar(512);
ALTER TABLE cases_history ALTER COLUMN vaccinationdoses TYPE varchar(512);
ALTER TABLE cases_history ALTER COLUMN vaccine TYPE varchar(512);
ALTER TABLE cases_history ALTER COLUMN epidnumber TYPE varchar(512);
ALTER TABLE cases_history ALTER COLUMN sequelaedetails TYPE varchar(512);
ALTER TABLE cases_history ALTER COLUMN notifyingclinicdetails TYPE varchar(512);
ALTER TABLE cases_history ALTER COLUMN pointofentrydetails TYPE varchar(512);
ALTER TABLE cases_history ALTER COLUMN externalid TYPE varchar(512);
ALTER TABLE cases_history ALTER COLUMN quarantinehelpneeded TYPE varchar(512);
ALTER TABLE cases_history ALTER COLUMN quarantinehomepossiblecomment TYPE varchar(512);
ALTER TABLE cases_history ALTER COLUMN quarantinehomesupplyensuredcomment TYPE varchar(512);

ALTER TABLE maternalhistory ALTER COLUMN othercomplicationsdetails TYPE varchar(512);
ALTER TABLE maternalhistory_history ALTER COLUMN othercomplicationsdetails TYPE varchar(512);

ALTER TABLE porthealthinfo ALTER COLUMN airlinename TYPE varchar(512);
ALTER TABLE porthealthinfo ALTER COLUMN flightnumber TYPE varchar(512);
ALTER TABLE porthealthinfo ALTER COLUMN seatnumber TYPE varchar(512);
ALTER TABLE porthealthinfo ALTER COLUMN departureairport TYPE varchar(512);
ALTER TABLE porthealthinfo ALTER COLUMN transitstopdetails1 TYPE varchar(512);
ALTER TABLE porthealthinfo ALTER COLUMN transitstopdetails2 TYPE varchar(512);
ALTER TABLE porthealthinfo ALTER COLUMN transitstopdetails3 TYPE varchar(512);
ALTER TABLE porthealthinfo ALTER COLUMN transitstopdetails4 TYPE varchar(512);
ALTER TABLE porthealthinfo ALTER COLUMN transitstopdetails5 TYPE varchar(512);
ALTER TABLE porthealthinfo ALTER COLUMN vesselname TYPE varchar(512);
ALTER TABLE porthealthinfo ALTER COLUMN vesseldetails TYPE varchar(512);
ALTER TABLE porthealthinfo ALTER COLUMN portofdeparture TYPE varchar(512);
ALTER TABLE porthealthinfo ALTER COLUMN lastportofcall TYPE varchar(512);
ALTER TABLE porthealthinfo ALTER COLUMN conveyancetypedetails TYPE varchar(512);
ALTER TABLE porthealthinfo ALTER COLUMN departurelocation TYPE varchar(512);
ALTER TABLE porthealthinfo ALTER COLUMN finaldestination TYPE varchar(512);
ALTER TABLE porthealthinfo_history ALTER COLUMN airlinename TYPE varchar(512);
ALTER TABLE porthealthinfo_history ALTER COLUMN flightnumber TYPE varchar(512);
ALTER TABLE porthealthinfo_history ALTER COLUMN seatnumber TYPE varchar(512);
ALTER TABLE porthealthinfo_history ALTER COLUMN departureairport TYPE varchar(512);
ALTER TABLE porthealthinfo_history ALTER COLUMN transitstopdetails1 TYPE varchar(512);
ALTER TABLE porthealthinfo_history ALTER COLUMN transitstopdetails2 TYPE varchar(512);
ALTER TABLE porthealthinfo_history ALTER COLUMN transitstopdetails3 TYPE varchar(512);
ALTER TABLE porthealthinfo_history ALTER COLUMN transitstopdetails4 TYPE varchar(512);
ALTER TABLE porthealthinfo_history ALTER COLUMN transitstopdetails5 TYPE varchar(512);
ALTER TABLE porthealthinfo_history ALTER COLUMN vesselname TYPE varchar(512);
ALTER TABLE porthealthinfo_history ALTER COLUMN vesseldetails TYPE varchar(512);
ALTER TABLE porthealthinfo_history ALTER COLUMN portofdeparture TYPE varchar(512);
ALTER TABLE porthealthinfo_history ALTER COLUMN lastportofcall TYPE varchar(512);
ALTER TABLE porthealthinfo_history ALTER COLUMN conveyancetypedetails TYPE varchar(512);
ALTER TABLE porthealthinfo_history ALTER COLUMN departurelocation TYPE varchar(512);
ALTER TABLE porthealthinfo_history ALTER COLUMN finaldestination TYPE varchar(512);

ALTER TABLE clinicalvisit ALTER COLUMN visitremarks TYPE varchar(512);
ALTER TABLE clinicalvisit ALTER COLUMN visitingperson TYPE varchar(512);
ALTER TABLE clinicalvisit_history ALTER COLUMN visitremarks TYPE varchar(512);
ALTER TABLE clinicalvisit_history ALTER COLUMN visitingperson TYPE varchar(512);

ALTER TABLE contact ALTER COLUMN diseasedetails TYPE varchar(512);
ALTER TABLE contact ALTER COLUMN relationdescription TYPE varchar(512);
ALTER TABLE contact ALTER COLUMN externalid TYPE varchar(512);
ALTER TABLE contact ALTER COLUMN immunosuppressiveTherapyBasicDiseaseDetails TYPE varchar(512);
ALTER TABLE contact ALTER COLUMN caseidexternalsystem TYPE varchar(512);
ALTER TABLE contact ALTER COLUMN contactproximitydetails TYPE varchar(512);
ALTER TABLE contact ALTER COLUMN quarantinehelpneeded TYPE varchar(512);
ALTER TABLE contact ALTER COLUMN quarantinehomepossiblecomment TYPE varchar(512);
ALTER TABLE contact ALTER COLUMN quarantinehomesupplyensuredcomment TYPE varchar(512);
ALTER TABLE contact_history ALTER COLUMN diseasedetails TYPE varchar(512);
ALTER TABLE contact_history ADD COLUMN relationdescription varchar(512);
ALTER TABLE contact_history ALTER COLUMN externalid TYPE varchar(512);
ALTER TABLE contact_history ALTER COLUMN immunosuppressiveTherapyBasicDiseaseDetails TYPE varchar(512);
ALTER TABLE contact_history ALTER COLUMN caseidexternalsystem TYPE varchar(512);
ALTER TABLE contact_history ALTER COLUMN contactproximitydetails TYPE varchar(512);
ALTER TABLE contact_history ALTER COLUMN quarantinehelpneeded TYPE varchar(512);
ALTER TABLE contact_history ALTER COLUMN quarantinehomepossiblecomment TYPE varchar(512);
ALTER TABLE contact_history ALTER COLUMN quarantinehomesupplyensuredcomment TYPE varchar(512);

ALTER TABLE epidata ALTER COLUMN otheranimalsdetails TYPE varchar(512);
ALTER TABLE epidata ALTER COLUMN watersourceother TYPE varchar(512);
ALTER TABLE epidata ALTER COLUMN waterbodydetails TYPE varchar(512);
ALTER TABLE epidata ALTER COLUMN kindofexposuredetails TYPE varchar(512);
ALTER TABLE epidata ALTER COLUMN placeoflastexposure TYPE varchar(512);
ALTER TABLE epidata ALTER COLUMN sickdeadanimalsdetails TYPE varchar(512);
ALTER TABLE epidata ALTER COLUMN sickdeadanimalslocation TYPE varchar(512);
ALTER TABLE epidata ALTER COLUMN eatingrawanimalsdetails TYPE varchar(512);
ALTER TABLE epidata_history ALTER COLUMN otheranimalsdetails TYPE varchar(512);
ALTER TABLE epidata_history ALTER COLUMN watersourceother TYPE varchar(512);
ALTER TABLE epidata_history ALTER COLUMN waterbodydetails TYPE varchar(512);
ALTER TABLE epidata_history ALTER COLUMN kindofexposuredetails TYPE varchar(512);
ALTER TABLE epidata_history ALTER COLUMN placeoflastexposure TYPE varchar(512);
ALTER TABLE epidata_history ALTER COLUMN sickdeadanimalsdetails TYPE varchar(512);
ALTER TABLE epidata_history ALTER COLUMN sickdeadanimalslocation TYPE varchar(512);
ALTER TABLE epidata_history ALTER COLUMN eatingrawanimalsdetails TYPE varchar(512);

ALTER TABLE epidataburial ALTER COLUMN burialpersonname TYPE varchar(512);
ALTER TABLE epidataburial ALTER COLUMN burialrelation TYPE varchar(512);
ALTER TABLE epidataburial_history ALTER COLUMN burialpersonname TYPE varchar(512);
ALTER TABLE epidataburial_history ALTER COLUMN burialrelation TYPE varchar(512);

ALTER TABLE epidatatravel ALTER COLUMN traveldestination TYPE varchar(512);
ALTER TABLE epidatatravel_history ALTER COLUMN traveldestination TYPE varchar(512);

ALTER TABLE events ALTER COLUMN srcfirstname TYPE varchar(512);
ALTER TABLE events ALTER COLUMN srclastname TYPE varchar(512);
ALTER TABLE events ALTER COLUMN srctelno TYPE varchar(512);
ALTER TABLE events ALTER COLUMN srcemail TYPE varchar(512);
ALTER TABLE events ALTER COLUMN diseasedetails TYPE varchar(512);
ALTER TABLE events ALTER COLUMN typeofplacetext TYPE varchar(512);
ALTER TABLE events_history ALTER COLUMN srcfirstname TYPE varchar(512);
ALTER TABLE events_history ALTER COLUMN srclastname TYPE varchar(512);
ALTER TABLE events_history ALTER COLUMN srctelno TYPE varchar(512);
ALTER TABLE events_history ALTER COLUMN srcemail TYPE varchar(512);
ALTER TABLE events_history ADD COLUMN diseasedetails varchar(512);
ALTER TABLE events_history ALTER COLUMN typeofplacetext TYPE varchar(512);

ALTER TABLE facility ALTER COLUMN city TYPE varchar(512);
ALTER TABLE facility ALTER COLUMN externalid TYPE varchar(512);

ALTER TABLE previoushospitalization ALTER COLUMN healthfacilitydetails TYPE varchar(512);
ALTER TABLE previoushospitalization_history ALTER COLUMN healthfacilitydetails TYPE varchar(512);

ALTER TABLE exportconfiguration ALTER COLUMN name TYPE varchar(512);
ALTER TABLE exportconfiguration_history ALTER COLUMN name TYPE varchar(512);

ALTER TABLE pointofentry ALTER COLUMN name TYPE varchar(512);
ALTER TABLE pointofentry ALTER COLUMN externalid TYPE varchar(512);
ALTER TABLE pointofentry ALTER COLUMN name TYPE varchar(512);
ALTER TABLE pointofentry ALTER COLUMN externalid TYPE varchar(512);

ALTER TABLE location ALTER COLUMN details TYPE varchar(512);
ALTER TABLE location ALTER COLUMN city TYPE varchar(512);
ALTER TABLE location ALTER COLUMN postalcode TYPE varchar(512);
ALTER TABLE location_history ALTER COLUMN details TYPE varchar(512);
ALTER TABLE location_history ALTER COLUMN city TYPE varchar(512);
ALTER TABLE location_history ALTER COLUMN postalcode TYPE varchar(512);

ALTER TABLE person ALTER COLUMN firstname TYPE varchar(512);
ALTER TABLE person ALTER COLUMN lastname TYPE varchar(512);
ALTER TABLE person ALTER COLUMN nickname TYPE varchar(512);
ALTER TABLE person ALTER COLUMN mothersname TYPE varchar(512);
ALTER TABLE person ALTER COLUMN fathersname TYPE varchar(512);
ALTER TABLE person ALTER COLUMN placeofbirthfacilitydetails TYPE varchar(512);
ALTER TABLE person ALTER COLUMN generalpractitionerdetails TYPE varchar(512);
ALTER TABLE person_history ALTER COLUMN firstname TYPE varchar(512);
ALTER TABLE person_history ALTER COLUMN lastname TYPE varchar(512);
ALTER TABLE person_history ALTER COLUMN nickname TYPE varchar(512);
ALTER TABLE person_history ALTER COLUMN mothersname TYPE varchar(512);
ALTER TABLE person_history ALTER COLUMN fathersname TYPE varchar(512);
ALTER TABLE person_history ALTER COLUMN placeofbirthfacilitydetails TYPE varchar(512);
ALTER TABLE person_history ALTER COLUMN generalpractitionerdetails TYPE varchar(512);

ALTER TABLE users ALTER COLUMN username TYPE varchar(512);
ALTER TABLE users ALTER COLUMN firstname TYPE varchar(512);
ALTER TABLE users ALTER COLUMN lastname TYPE varchar(512);
ALTER TABLE users_history ALTER COLUMN username TYPE varchar(512);
ALTER TABLE users_history ALTER COLUMN firstname TYPE varchar(512);
ALTER TABLE users_history ALTER COLUMN lastname TYPE varchar(512);

ALTER TABLE community ALTER COLUMN externalid TYPE varchar(512);
ALTER TABLE district ALTER COLUMN externalid TYPE varchar(512);
ALTER TABLE region ALTER COLUMN externalid TYPE varchar(512);

ALTER TABLE pathogentest ALTER COLUMN testeddiseasedetails TYPE varchar(512);
ALTER TABLE pathogentest ALTER COLUMN testtypetext TYPE varchar(512);
ALTER TABLE pathogentest ALTER COLUMN labdetails TYPE varchar(512);
ALTER TABLE pathogentest ALTER COLUMN serotype TYPE varchar(512);
ALTER TABLE pathogentest_history ALTER COLUMN testeddiseasedetails TYPE varchar(512);
ALTER TABLE pathogentest_history ALTER COLUMN testtypetext TYPE varchar(512);
ALTER TABLE pathogentest_history ALTER COLUMN labdetails TYPE varchar(512);
ALTER TABLE pathogentest_history ALTER COLUMN serotype TYPE varchar(512);

ALTER TABLE samples ALTER COLUMN labsampleid TYPE varchar(512);
ALTER TABLE samples ALTER COLUMN fieldsampleid TYPE varchar(512);
ALTER TABLE samples ALTER COLUMN samplematerialtext TYPE varchar(512);
ALTER TABLE samples ALTER COLUMN labdetails TYPE varchar(512);
ALTER TABLE samples ALTER COLUMN shipmentdetails TYPE varchar(512);
ALTER TABLE samples ALTER COLUMN notestpossiblereason TYPE varchar(512);
ALTER TABLE samples ALTER COLUMN requestedotherpathogentests TYPE varchar(512);
ALTER TABLE samples ALTER COLUMN requestedotheradditionaltests TYPE varchar(512);
ALTER TABLE samples_history ALTER COLUMN labsampleid TYPE varchar(512);
ALTER TABLE samples_history ALTER COLUMN fieldsampleid TYPE varchar(512);
ALTER TABLE samples_history ALTER COLUMN samplematerialtext TYPE varchar(512);
ALTER TABLE samples_history ALTER COLUMN labdetails TYPE varchar(512);
ALTER TABLE samples_history ALTER COLUMN shipmentdetails TYPE varchar(512);
ALTER TABLE samples_history ALTER COLUMN notestpossiblereason TYPE varchar(512);
ALTER TABLE samples_history ALTER COLUMN requestedotherpathogentests TYPE varchar(512);
ALTER TABLE samples_history ALTER COLUMN requestedotheradditionaltests TYPE varchar(512);

ALTER TABLE symptoms ALTER COLUMN patientilllocation TYPE varchar(512);
ALTER TABLE symptoms ALTER COLUMN onsetsymptom TYPE varchar(512);
ALTER TABLE symptoms ALTER COLUMN otherhemorrhagicsymptomstext TYPE varchar(512);
ALTER TABLE symptoms ALTER COLUMN othernonhemorrhagicsymptomstext TYPE varchar(512);
ALTER TABLE symptoms ALTER COLUMN congenitalheartdiseasedetails TYPE varchar(512);
ALTER TABLE symptoms ALTER COLUMN symptomscomments TYPE varchar(512);
ALTER TABLE symptoms ALTER COLUMN otherComplicationsText TYPE varchar(512);
ALTER TABLE symptoms_history ALTER COLUMN patientilllocation TYPE varchar(512);
ALTER TABLE symptoms_history ALTER COLUMN onsetsymptom TYPE varchar(512);
ALTER TABLE symptoms_history ALTER COLUMN otherhemorrhagicsymptomstext TYPE varchar(512);
ALTER TABLE symptoms_history ALTER COLUMN othernonhemorrhagicsymptomstext TYPE varchar(512);
ALTER TABLE symptoms_history ALTER COLUMN congenitalheartdiseasedetails TYPE varchar(512);
ALTER TABLE symptoms_history ALTER COLUMN symptomscomments TYPE varchar(512);
ALTER TABLE symptoms_history ALTER COLUMN otherComplicationsText TYPE varchar(512);

ALTER TABLE prescription ALTER COLUMN prescribingclinician TYPE varchar(512);
ALTER TABLE prescription ALTER COLUMN prescriptiondetails TYPE varchar(512);
ALTER TABLE prescription ALTER COLUMN frequency TYPE varchar(512);
ALTER TABLE prescription ALTER COLUMN dose TYPE varchar(512);
ALTER TABLE prescription ALTER COLUMN routedetails TYPE varchar(512);
ALTER TABLE prescription_history ALTER COLUMN prescribingclinician TYPE varchar(512);
ALTER TABLE prescription_history ALTER COLUMN prescriptiondetails TYPE varchar(512);
ALTER TABLE prescription_history ALTER COLUMN frequency TYPE varchar(512);
ALTER TABLE prescription_history ALTER COLUMN dose TYPE varchar(512);
ALTER TABLE prescription_history ALTER COLUMN routedetails TYPE varchar(512);

ALTER TABLE treatment ALTER COLUMN executingclinician TYPE varchar(512);
ALTER TABLE treatment ALTER COLUMN treatmentdetails TYPE varchar(512);
ALTER TABLE treatment ALTER COLUMN dose TYPE varchar(512);
ALTER TABLE treatment ALTER COLUMN routedetails TYPE varchar(512);
ALTER TABLE treatment_history ALTER COLUMN executingclinician TYPE varchar(512);
ALTER TABLE treatment_history ALTER COLUMN treatmentdetails TYPE varchar(512);
ALTER TABLE treatment_history ALTER COLUMN dose TYPE varchar(512);
ALTER TABLE treatment_history ALTER COLUMN routedetails TYPE varchar(512);

ALTER TABLE visit ALTER COLUMN visitremarks TYPE varchar(512);
ALTER TABLE visit_history ALTER COLUMN visitremarks TYPE varchar(512);

INSERT INTO schema_version (version_number, comment) VALUES (214, 'Change text field lengths #580');

-- 2020-06-10 Remove wrongly assigned surveillance officers from cases #2284
UPDATE cases SET surveillanceofficer_id = null FROM users WHERE cases.surveillanceofficer_id = users.id AND cases.district_id != users.district_id;

INSERT INTO schema_version (version_number, comment) VALUES (215, 'Remove wrongly assigned surveillance officers from cases #2284');

-- 2020-06-18 Add campaign forms #2268
CREATE TABLE campaignforms(
	id bigint not null,
	uuid varchar(36) not null unique,
	changedate timestamp not null,
	creationdate timestamp not null,
	formid varchar(255),
	languagecode varchar(255),
	campaignformelements text,
	campaignformtranslations text,
	sys_period tstzrange not null,
	primary key(id)
);

ALTER TABLE campaignforms OWNER TO sormas_user;

CREATE TABLE campaignforms_history (LIKE campaigns);
CREATE TRIGGER versioning_trigger BEFORE INSERT OR UPDATE OR DELETE ON campaignforms
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'campaignforms_history', true);
ALTER TABLE campaignforms_history OWNER TO sormas_user;

INSERT INTO schema_version (version_number, comment) VALUES (216, 'Add campaign forms #2268');

-- 2020-06-19 Add Area as new infrastructure type #1983
CREATE TABLE areas(
	id bigint not null,
	uuid varchar(36) not null unique,
	changedate timestamp not null,
	creationdate timestamp not null,
	name varchar(512),
	externalid varchar(512),
	archived boolean DEFAULT false,
	sys_period tstzrange not null,
	primary key(id)
);

ALTER TABLE areas OWNER TO sormas_user;

CREATE TABLE areas_history (LIKE areas);
CREATE TRIGGER versioning_trigger BEFORE INSERT OR UPDATE OR DELETE ON areas
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'areas_history', true);
ALTER TABLE areas_history OWNER TO sormas_user;

ALTER TABLE region ADD COLUMN area_id bigint;
ALTER TABLE region ADD CONSTRAINT fk_region_area_id FOREIGN KEY (area_id) REFERENCES areas(id);

INSERT INTO schema_version (version_number, comment) VALUES (217, 'Add Area as new infrastructure type #1983');

CREATE TABLE campaignformdata(
	id bigint not null,
	uuid varchar(36) not null unique,
	changedate timestamp not null,
	creationdate timestamp not null,
	formData text,
	campaign_id bigint NOT NULL,
	campaignform_id bigint NOT NULL,
	region_id bigint NOT NULL,
	district_id bigint NOT NULL,
	community_id bigint,
	sys_period tstzrange not null,
	primary key(id)
);
ALTER TABLE campaignformdata OWNER TO sormas_user;
CREATE TABLE campaignformdata_history (LIKE campaignformdata);
CREATE TRIGGER versioning_trigger BEFORE INSERT OR UPDATE OR DELETE ON campaignformdata
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'campaignformdata_history', true);
ALTER TABLE campaignformdata_history OWNER TO sormas_user;

INSERT INTO schema_version (version_number, comment) VALUES (218, 'Add campaignformdata #1992');

-- 2020-06-30 Add "Other" and a text field to QuarantineType #2219
ALTER TABLE cases ADD COLUMN quarantinetypedetails varchar(512);
ALTER TABLE contact ADD COLUMN quarantinetypedetails varchar(512);

ALTER TABLE cases_history ADD COLUMN quarantinetypedetails varchar(512);
ALTER TABLE contact_history ADD COLUMN quarantinetypedetails varchar(512);

INSERT INTO schema_version (version_number, comment) VALUES (219, 'Add "Other" and a text field to QuarantineType #2219');

-- 2020-06-29 Add samples to event participants #2395
ALTER TABLE samples
    ADD COLUMN associatedeventparticipant_id bigint;
ALTER TABLE samples
    ADD CONSTRAINT fk_samples_associatedeventparticipant_id FOREIGN KEY (associatedeventparticipant_id) REFERENCES eventparticipant (id);
ALTER TABLE samples_history
    ADD COLUMN associatedeventparticipant_id bigint;

INSERT INTO schema_version (version_number, comment) VALUES (220, 'Add samples to event participants #2395');

-- 2020-06-29 Extend event details #2391
UPDATE events set eventstatus='SIGNAL' where eventstatus='POSSIBLE';
UPDATE events set eventstatus='EVENT' where eventstatus='CONFIRMED';
UPDATE events set eventstatus='DROPPED' where eventstatus='NO_EVENT';

ALTER TABLE events RENAME COLUMN eventdate TO startdate;
ALTER TABLE events ADD COLUMN enddate timestamp;
ALTER TABLE events ADD COLUMN externalId varchar(512);
ALTER TABLE events ADD COLUMN nosocomial varchar(255);
ALTER TABLE events ADD COLUMN srcType varchar(255);
ALTER TABLE events ADD COLUMN srcMediaWebsite varchar(512);
ALTER TABLE events ADD COLUMN srcMediaName varchar(512);
ALTER TABLE events ADD COLUMN srcMediaDetails varchar(4096);

UPDATE events set srcType='HOTLINE_PERSON' where LENGTH(CONCAT(srcfirstname, srclastname, srctelno, srcemail)) > 0;

INSERT INTO schema_version (version_number, comment) VALUES (221, 'Extend event details #2391');

-- 2020-06-18 Remove wrongly assigned surveillance officers from cases #2284
ALTER TABLE contact ADD COLUMN epidata_id bigint;
ALTER TABLE contact_history ADD COLUMN epidata_id bigint;
ALTER TABLE contact ADD CONSTRAINT fk_contact_epidata_id FOREIGN KEY (epidata_id) REFERENCES epidata(id);

DO $$
    DECLARE rec RECORD;
        DECLARE new_epidata_id INTEGER;
    BEGIN
        FOR rec IN SELECT id FROM public.contact WHERE epidata_id IS NULL
            LOOP
                INSERT INTO epidata(id, uuid, creationdate, changedate) VALUES (nextval('entity_seq'), upper(substring(CAST(CAST(md5(CAST(random() AS text) || CAST(clock_timestamp() AS text)) AS uuid) AS text), 3, 29)), now(), now()) RETURNING id INTO new_epidata_id;
                UPDATE contact SET epidata_id = new_epidata_id WHERE id = rec.id;
            END LOOP;
    END;
$$ LANGUAGE plpgsql;

INSERT INTO schema_version (version_number, comment) VALUES (222, 'Add Epidemiological data to contacts');

-- 2020-07-02 Rename formData field #2268
ALTER TABLE campaignformdata RENAME formData TO formvalues;
ALTER TABLE campaignformdata_history RENAME formData to formvalues;

INSERT INTO schema_version (version_number, comment) VALUES (223, 'Rename formData field #2268');

-- 2020-07-10 Add archived column to campaign form data #2268
ALTER TABLE campaignformdata ADD COLUMN archived boolean NOT NULL DEFAULT false;
ALTER TABLE campaignformdata_history ADD COLUMN archived boolean;

INSERT INTO schema_version (version_number, comment) VALUES (224, 'Add archived column to campaign form data #2268');

-- 2020-07-15 Add form date to campaign form data #1997
ALTER TABLE campaignformdata ADD COLUMN formdate timestamp;
ALTER TABLE campaignformdata_history ADD COLUMN formdate timestamp;

INSERT INTO schema_version (version_number, comment) VALUES (225, 'Add form date to campaign form data #1997');

-- 2020-07-03 Add case classification for Germany #2230
ALTER TABLE cases ADD COLUMN clinicalconfirmation varchar(255);
ALTER TABLE cases ADD COLUMN epidemiologicalconfirmation varchar(255);
ALTER TABLE cases ADD COLUMN laboratorydiagnosticconfirmation varchar(255);
ALTER TABLE cases_history ADD COLUMN clinicalconfirmation varchar(255);
ALTER TABLE cases_history ADD COLUMN epidemiologicalconfirmation varchar(255);
ALTER TABLE cases_history ADD COLUMN laboratorydiagnosticconfirmation varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (226, 'Add case classification for Germany #2230');

-- 2020-07-16 Add source of identification as contact to contacts #2070
ALTER TABLE contact ADD COLUMN contactidentificationsource varchar(255);
ALTER TABLE contact ADD COLUMN contactidentificationsourcedetails varchar(512);
ALTER TABLE contact ADD COLUMN tracingapp varchar(255);
ALTER TABLE contact ADD COLUMN tracingappdetails varchar(512);
ALTER TABLE contact_history ADD COLUMN contactidentificationsource varchar(255);
ALTER TABLE contact_history ADD COLUMN contactidentificationsourcedetails varchar(512);
ALTER TABLE contact_history ADD COLUMN tracingapp varchar(255);
ALTER TABLE contact_history ADD COLUMN tracingappdetails varchar(512);

INSERT INTO schema_version (version_number, comment) VALUES (227, 'Add source of identification as contact to contacts #2070');

-- 2020-07-27 Add form name to campaign forms and creating user to form data #1993
ALTER TABLE campaignforms ADD COLUMN formname varchar(512);
ALTER TABLE campaignforms_history ADD COLUMN formname varchar(512);
ALTER TABLE campaignformdata ADD COLUMN creatinguser_id bigint;
ALTER TABLE campaignformdata_history ADD COLUMN creatinguser_id bigint;

ALTER TABLE campaignformdata ADD CONSTRAINT fk_campaignformdata_creatinguser_id FOREIGN KEY (creatinguser_id) REFERENCES users(id);

INSERT INTO schema_version (version_number, comment) VALUES (228, 'Add form name to campaign forms and creating user to form data #1993');

-- 2020-07-27 Rename campaignforms to campaignformmeta #1997
ALTER TABLE campaignforms RENAME TO campaignformmeta;
ALTER TABLE campaignforms_history RENAME TO campaignformmeta_history;
ALTER TABLE campaignformdata RENAME COLUMN campaignform_id TO campaignformmeta_id;
ALTER TABLE campaignformdata_history RENAME COLUMN campaignform_id TO campaignformmeta_id;

ALTER TABLE campaignformdata ADD CONSTRAINT fk_campaignformdata_campaign_id FOREIGN KEY (campaign_id) REFERENCES campaigns(id);
ALTER TABLE campaignformdata ADD CONSTRAINT fk_campaignformdata_campaignformmeta_id FOREIGN KEY (campaignformmeta_id) REFERENCES campaignformmeta(id);
ALTER TABLE campaignformdata ADD CONSTRAINT fk_campaignformdata_region_id FOREIGN KEY (region_id) REFERENCES region(id);
ALTER TABLE campaignformdata ADD CONSTRAINT fk_campaignformdata_district_id FOREIGN KEY (district_id) REFERENCES district(id);
ALTER TABLE campaignformdata ADD CONSTRAINT fk_campaignformdata_community_id FOREIGN KEY (community_id) REFERENCES community(id);

INSERT INTO schema_version (version_number, comment) VALUES (229, 'Rename campaignforms to campaignformmeta #1997');

-- 2020-07-27 Drop and re-create versioning trigger for campaignformmeta #1997
DROP TRIGGER versioning_trigger ON campaignformmeta;
CREATE TRIGGER versioning_trigger BEFORE INSERT OR UPDATE OR DELETE ON campaignformmeta
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'campaignformmeta_history', true);

INSERT INTO schema_version (version_number, comment) VALUES (230, 'Drop and re-create versioning trigger for campaignformmeta #1997');

-- 2020-07-27 Add list elements to campaignformmeta #2515
ALTER TABLE campaignformmeta ADD COLUMN campaignformlistelements varchar(4096);
ALTER TABLE campaignformmeta_history ADD COLUMN campaignformlistelements varchar(4096);

INSERT INTO schema_version (version_number, comment) VALUES (231, 'Add list elements to campaignformmeta #2515');

-- 2020-06-10 Add actions

CREATE TABLE action (
id bigint not null,
reply varchar(4096),
changedate timestamp not null,
creationdate timestamp not null,
description varchar(4096),
date timestamp,
statuschangedate timestamp,
actioncontext varchar(512),
actionstatus varchar(512),
uuid varchar(36) not null unique,
event_id bigint,
creatoruser_id bigint,
priority varchar(512),
replyinguser_id bigint,
sys_period tstzrange not null,
PRIMARY KEY (id));

ALTER TABLE action OWNER TO sormas_user;

ALTER TABLE action ADD CONSTRAINT fk_action_event_id FOREIGN KEY (event_id) REFERENCES events (id);
ALTER TABLE action ADD CONSTRAINT fk_action_creatoruser_id FOREIGN KEY (creatoruser_id) REFERENCES users (id);

UPDATE action SET sys_period=tstzrange(creationdate, null);
ALTER TABLE action ALTER COLUMN sys_period SET NOT NULL;
CREATE TABLE action_history (LIKE action);
CREATE TRIGGER versioning_trigger
BEFORE INSERT OR UPDATE OR DELETE ON action
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'action_history', true);
ALTER TABLE action_history OWNER TO sormas_user;

INSERT INTO schema_version (version_number, comment) VALUES (232, 'Adds actions to events');

-- 2020-07-29 - Remove list elements from campaignformmeta #2515
ALTER TABLE campaignformmeta DROP COLUMN campaignformlistelements;
ALTER TABLE campaignformmeta_history DROP COLUMN campaignformlistelements;

INSERT INTO schema_version (version_number, comment) VALUES (233, 'Remove list elements from campaignformmeta #2515');

-- 2020-07-29 Campaign diagram definition
CREATE TABLE campaigndiagramdefinition(
                              id bigint not null,
                              uuid varchar(36) not null unique,
                              changedate timestamp not null,
                              creationdate timestamp not null,
                              diagramId varchar(255) not null unique,
                              diagramType varchar(255),
                              campaignDiagramSeries text,
                              sys_period tstzrange not null,
                              primary key(id)
);

ALTER TABLE campaigndiagramdefinition OWNER TO sormas_user;

CREATE TABLE campaigndiagramdefinition_history (LIKE campaigndiagramdefinition);
CREATE TRIGGER versioning_trigger BEFORE INSERT OR UPDATE OR DELETE ON campaigndiagramdefinition
    FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'campaigndiagramdefinition_history', true);
ALTER TABLE campaigndiagramdefinition_history OWNER TO sormas_user;

INSERT INTO schema_version (version_number, comment) VALUES (234, 'Campaign diagram definition');

-- 2020-07-30 - Store if quarantine period has been extended #2264
ALTER TABLE cases ADD COLUMN quarantineextended boolean DEFAULT false;
ALTER TABLE contact ADD COLUMN quarantineextended boolean DEFAULT false;

ALTER TABLE cases_history ADD COLUMN quarantineextended boolean DEFAULT false;
ALTER TABLE contact_history ADD COLUMN quarantineextended boolean DEFAULT false;

INSERT INTO schema_version (version_number, comment) VALUES (235, 'Store if quarantine period has been extended #2264');

-- 2020-08-10 Add responsible community to contact #2104
ALTER TABLE contact ADD COLUMN community_id bigint;
ALTER TABLE contact_history ADD COLUMN community_id bigint;
ALTER TABLE contact ADD CONSTRAINT community_id FOREIGN KEY (community_id) REFERENCES community (id);

INSERT INTO schema_version (version_number, comment) VALUES (236, 'Add responsible community to contact #2104');

-- 2020-08-13 Adds visit to cases

ALTER TABLE cases ADD COLUMN followupstatus varchar(255);
ALTER TABLE cases ADD COLUMN followupcomment varchar(4096);
ALTER TABLE cases ADD COLUMN followupuntil timestamp;
ALTER TABLE cases ADD COLUMN overwritefollowupuntil boolean;

UPDATE cases SET followupstatus = 'CANCELED';
UPDATE cases SET followupcomment = '-';
UPDATE cases SET overwritefollowupuntil = false;

ALTER TABLE cases_history ADD COLUMN followupstatus varchar(255);
ALTER TABLE cases_history ADD COLUMN followupcomment varchar(4096);
ALTER TABLE cases_history ADD COLUMN followupuntil timestamp;
ALTER TABLE cases_history ADD COLUMN overwritefollowupuntil boolean;

ALTER TABLE visit ADD COLUMN caze_id bigint;
ALTER TABLE visit_history ADD COLUMN caze_id bigint;

INSERT INTO schema_version (version_number, comment) VALUES (237, 'Adds visit to cases');

-- 2020-08-10 - Update app synchronization related to event participants #2596
ALTER TABLE  eventparticipant ADD COLUMN deleted boolean;
ALTER TABLE  eventparticipant_history ADD COLUMN deleted boolean;
UPDATE eventparticipant SET deleted = false;
UPDATE eventparticipant_history SET deleted = false;

INSERT INTO schema_version (version_number, comment) VALUES (238, 'Update app synchronization related to event participants #2596');

-- 2020-06-23 Import and use new facility types #1637
UPDATE samples SET lab_id = (SELECT id FROM facility WHERE uuid = 'SORMAS-CONSTID-OTHERS-FACILITY') WHERE lab_id = (SELECT id FROM facility WHERE uuid = 'SORMAS-CONSTID-OTHERS-LABORATO');
UPDATE pathogentest SET lab_id = (SELECT id FROM facility WHERE uuid = 'SORMAS-CONSTID-OTHERS-FACILITY') WHERE lab_id = (SELECT id FROM facility WHERE uuid = 'SORMAS-CONSTID-OTHERS-LABORATO');
DELETE FROM facility WHERE uuid = 'SORMAS-CONSTID-OTHERS-LABORATO';
UPDATE facility SET type = 'HOSPITAL' WHERE NOT type = 'LABORATORY' AND uuid NOT IN ('SORMAS-CONSTID-OTHERS-FACILITY','SORMAS-CONSTID-ISNONE-FACILITY');
ALTER TABLE cases ADD COLUMN facilitytype varchar(255);
ALTER TABLE cases_history ADD COLUMN facilitytype varchar(255);
UPDATE cases SET facilitytype = 'HOSPITAL' WHERE healthfacility_id != (SELECT id FROM facility WHERE uuid = 'SORMAS-CONSTID-ISNONE-FACILITY');
ALTER TABLE person ADD COLUMN occupationfacilitytype varchar(255);
ALTER TABLE person_history ADD COLUMN occupationfacilitytype varchar(255);
UPDATE person SET occupationfacilitytype = 'HOSPITAL' WHERE occupationfacility_id IS NOT NULL;
ALTER TABLE person ADD COLUMN placeofbirthfacilitytype varchar(255);
ALTER TABLE person_history ADD COLUMN placeofbirthfacilitytype varchar(255);
UPDATE person SET placeofbirthfacilitytype = 'HOSPITAL' WHERE placeofbirthfacility_id IS NOT NULL;

INSERT INTO schema_version (version_number, comment) VALUES (239, 'Import and use new facility types #1637');

-- 2020-08-20 Adjust Covid-19 Symptoms for Switzerland #2669
ALTER TABLE symptoms ADD COLUMN feverishFeeling varchar(255);
ALTER TABLE symptoms ADD COLUMN weakness varchar(255);
ALTER TABLE symptoms ADD COLUMN fatigue varchar(255);
ALTER TABLE symptoms ADD COLUMN coughWithoutSputum varchar(255);
ALTER TABLE symptoms ADD COLUMN breathlessness varchar(255);
ALTER TABLE symptoms ADD COLUMN chestPressure varchar(255);
ALTER TABLE symptoms ADD COLUMN blueLips varchar(255);
ALTER TABLE symptoms ADD COLUMN bloodCirculationProblems varchar(255);
ALTER TABLE symptoms ADD COLUMN palpitations varchar(255);
ALTER TABLE symptoms ADD COLUMN dizzinessStandingUp varchar(255);
ALTER TABLE symptoms ADD COLUMN highOrLowBloodPressure varchar(255);
ALTER TABLE symptoms ADD COLUMN urinaryRetention varchar(255);

ALTER TABLE symptoms_history ADD COLUMN feverishFeeling varchar(255);
ALTER TABLE symptoms_history ADD COLUMN weakness varchar(255);
ALTER TABLE symptoms_history ADD COLUMN fatigue varchar(255);
ALTER TABLE symptoms_history ADD COLUMN coughWithoutSputum varchar(255);
ALTER TABLE symptoms_history ADD COLUMN breathlessness varchar(255);
ALTER TABLE symptoms_history ADD COLUMN chestPressure varchar(255);
ALTER TABLE symptoms_history ADD COLUMN blueLips varchar(255);
ALTER TABLE symptoms_history ADD COLUMN bloodCirculationProblems varchar(255);
ALTER TABLE symptoms_history ADD COLUMN palpitations varchar(255);
ALTER TABLE symptoms_history ADD COLUMN dizzinessStandingUp varchar(255);
ALTER TABLE symptoms_history ADD COLUMN highOrLowBloodPressure varchar(255);
ALTER TABLE symptoms_history ADD COLUMN urinaryRetention varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (240, 'Adjust Covid-19 Symptoms for Switzerland #2669');

-- 2020-08-19 - Add pre-existing conditions to contacts #2564 - update healthconditions table
ALTER TABLE  contact ADD COLUMN healthconditions_id bigint;
ALTER TABLE contact ADD CONSTRAINT fk_contact_healthconditions_id FOREIGN KEY (healthconditions_id) REFERENCES healthconditions (id);

DO $$
    DECLARE rec RECORD;
    DECLARE new_healthcondition_id INTEGER;
    BEGIN
        UPDATE contact SET healthconditions_id = (SELECT hc.id FROM healthconditions hc
                                                                                inner join clinicalcourse cc on cc.healthconditions_id = hc.id
                                                                                inner join cases ca on ca.clinicalcourse_id = cc.id
                                                          where ca.id = resultingcase_id)
        WHERE resultingcase_id IS NOT NULL AND healthconditions_id IS NULL;

        FOR rec IN SELECT id FROM public.contact where resultingcase_id IS NULL and healthconditions_id IS NULL
            LOOP
                INSERT INTO healthconditions(id, uuid, creationdate, changedate) VALUES (nextval('entity_seq'), upper(substring(CAST(CAST(md5(CAST(random() AS text) || CAST(clock_timestamp() AS text)) AS uuid) AS text), 3, 29)), now(), now()) RETURNING id INTO new_healthcondition_id;
                UPDATE contact SET healthconditions_id = new_healthcondition_id WHERE id = rec.id;
            END LOOP;
    END;
$$ LANGUAGE plpgsql;
INSERT INTO schema_version (version_number, comment) VALUES (241, 'update healthconditions table #2564');

-- 2020-08-24 Fix problems caused by #1637
UPDATE facility SET type = 'HOSPITAL' WHERE type ISNULL AND uuid NOT IN ('SORMAS-CONSTID-OTHERS-FACILITY','SORMAS-CONSTID-ISNONE-FACILITY');
UPDATE cases SET facilitytype = null WHERE healthfacility_id ISNULL;

INSERT INTO schema_version (version_number, comment) VALUES (242, 'Fix problems caused by #1637');

-- 2020-07-29 Campaign diagram visualisation

ALTER TABLE campaigndiagramdefinition ALTER COLUMN campaignDiagramSeries TYPE json USING campaignDiagramSeries::json;
ALTER TABLE campaigndiagramdefinition_history ALTER COLUMN campaignDiagramSeries TYPE json USING campaignDiagramSeries::json;
ALTER TABLE campaignformdata ALTER COLUMN formvalues TYPE json USING formvalues::json;
ALTER TABLE campaignformdata_history ALTER COLUMN formvalues TYPE json USING formvalues::json;
ALTER TABLE campaigndiagramdefinition ADD COLUMN diagramCaption varchar(255);
ALTER TABLE campaigndiagramdefinition_history ADD COLUMN diagramCaption varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (243, 'Campaign diagram visualization #2526');

-- 2020-08-25 Person address refinement #2562
ALTER TABLE location RENAME COLUMN address TO street;
ALTER TABLE location_history RENAME COLUMN address TO street;
ALTER TABLE location ADD COLUMN housenumber varchar(255);
ALTER TABLE location_history ADD COLUMN housenumber varchar(255);
ALTER TABLE location ADD COLUMN additionalinformation varchar(255);
ALTER TABLE location_history ADD COLUMN additionalinformation varchar(255);
ALTER TABLE location ADD COLUMN addresstype varchar(255);
ALTER TABLE location_history ADD COLUMN addresstype varchar(255);
ALTER TABLE location ADD COLUMN addresstypedetails varchar(255);
ALTER TABLE location_history ADD COLUMN addresstypedetails varchar(255);
ALTER TABLE location ADD COLUMN person_id bigint;
ALTER TABLE location_history ADD COLUMN person_id bigint;
ALTER TABLE location ADD CONSTRAINT fk_location_person_id FOREIGN KEY (person_id) REFERENCES person (id);

ALTER TABLE person ADD COLUMN changedateofembeddedlists timestamp without time zone;

INSERT INTO schema_version (version_number, comment) VALUES (244, 'Person address refinement #2562');

-- 2020-08-28 Clone symptoms and epi data linked to cases and contacts/visits at the same time #2735

CREATE OR REPLACE FUNCTION create_new_uuid(uuid varchar) RETURNS varchar
AS 'SELECT LEFT(uuid, 21) ||
CASE SUBSTRING(uuid FROM 22 FOR 1)
WHEN ''9'' THEN ''A''
WHEN ''Z'' THEN ''0''
ELSE chr(ascii(SUBSTRING(uuid FROM 22 FOR 1)) + 1)
END ||
SUBSTRING(uuid FROM 23)
AS new_uuid;'
LANGUAGE SQL;

ALTER FUNCTION create_new_uuid(varchar) OWNER TO sormas_user;

-- EPI DATA

DROP TABLE IF EXISTS t_epidata;
DROP TABLE IF EXISTS t_epidataburial;
DROP TABLE IF EXISTS t_epidatagathering;
DROP TABLE IF EXISTS t_epidatatravel;
DROP TABLE IF EXISTS t_id_map;
DROP TABLE IF EXISTS t_edb_id_map;
DROP TABLE IF EXISTS t_edg_id_map;
DROP TABLE IF EXISTS t_edt_id_map;
DROP TABLE IF EXISTS t_epidataburial_location;
DROP TABLE IF EXISTS t_epidatagathering_location;
DROP TABLE IF EXISTS t_edbl_id_map;
DROP TABLE IF EXISTS t_edgl_id_map;

CREATE temp table t_epidata
AS SELECT e.* FROM epidata e WHERE e.id IN (SELECT ca.epidata_id FROM cases ca) AND e.id IN (SELECT co.epidata_id FROM contact co);

CREATE temp table t_id_map
AS SELECT id AS old_id,
nextval('entity_seq') AS new_id,
create_new_uuid(uuid) AS new_uuid
FROM t_epidata;

UPDATE t_epidata te SET
id = (SELECT new_id FROM t_id_map WHERE te.id = old_id),
uuid = (SELECT new_uuid FROM t_id_map WHERE te.id = old_id);

-- BURIALS

CREATE temp table t_epidataburial
AS SELECT edb.* FROM epidataburial edb, t_id_map WHERE edb.epidata_id = t_id_map.old_id;

CREATE temp table t_edb_id_map
AS SELECT id AS edb_old_id,
nextval('entity_seq') AS edb_new_id,
create_new_uuid(uuid) AS edb_new_uuid
FROM t_epidataburial;

CREATE temp table t_epidataburial_location
AS SELECT edbl.* FROM location edbl, t_epidataburial edb WHERE edb.burialaddress_id = edbl.id;

CREATE temp table t_edbl_id_map
AS SELECT id AS edbl_old_id,
nextval('entity_seq') AS edbl_new_id,
create_new_uuid(uuid) AS edbl_new_uuid
FROM t_epidataburial_location;

UPDATE t_epidataburial_location tedbl SET
id = (SELECT edbl_new_id FROM t_edbl_id_map WHERE tedbl.id = edbl_old_id),
uuid = (SELECT edbl_new_uuid FROM t_edbl_id_map WHERE tedbl.id = edbl_old_id);

INSERT INTO location (SELECT * FROM t_epidataburial_location);

UPDATE t_epidataburial tedb SET
id = (SELECT edb_new_id FROM t_edb_id_map WHERE tedb.id = edb_old_id),
uuid = (SELECT edb_new_uuid FROM t_edb_id_map WHERE tedb.id = edb_old_id),
epidata_id = (SELECT new_id FROM t_id_map WHERE tedb.epidata_id = old_id),
burialaddress_id = (SELECT edbl_new_id FROM t_edbl_id_map WHERE tedb.burialaddress_id = edbl_old_id);

-- BURIALS END

-- GATHERINGS

CREATE temp table t_epidatagathering
AS SELECT edg.* FROM epidatagathering edg, t_id_map WHERE edg.epidata_id = t_id_map.old_id;

CREATE temp table t_edg_id_map
AS SELECT id AS edg_old_id,
nextval('entity_seq') AS edg_new_id,
create_new_uuid(uuid) AS edg_new_uuid
FROM t_epidatagathering;

CREATE temp table t_epidatagathering_location
AS SELECT edgl.* FROM location edgl, t_epidatagathering edg WHERE edg.gatheringaddress_id = edgl.id;

CREATE temp table t_edgl_id_map
AS SELECT id AS edgl_old_id,
nextval('entity_seq') AS edgl_new_id,
create_new_uuid(uuid) AS edgl_new_uuid
FROM t_epidatagathering_location;

UPDATE t_epidatagathering_location tedgl SET
id = (SELECT edgl_new_id FROM t_edgl_id_map WHERE tedgl.id = edgl_old_id),
uuid = (SELECT edgl_new_uuid FROM t_edgl_id_map WHERE tedgl.id = edgl_old_id);

INSERT INTO location (SELECT * FROM t_epidatagathering_location);

UPDATE t_epidatagathering tedg SET
id = (SELECT edg_new_id FROM t_edg_id_map WHERE tedg.id = edg_old_id),
uuid = (SELECT edg_new_uuid FROM t_edg_id_map WHERE tedg.id = edg_old_id),
epidata_id = (SELECT new_id FROM t_id_map WHERE tedg.epidata_id = old_id),
gatheringaddress_id = (SELECT edgl_new_id FROM t_edgl_id_map WHERE tedg.gatheringaddress_id = edgl_old_id);

-- GATHERINGS END

-- TRAVELS

CREATE temp table t_epidatatravel
AS SELECT edt.* FROM epidatatravel edt, t_id_map WHERE edt.epidata_id = t_id_map.old_id;

CREATE temp table t_edt_id_map
AS SELECT id AS edt_old_id,
nextval('entity_seq') AS edt_new_id,
create_new_uuid(uuid) AS edt_new_uuid
FROM t_epidatatravel;

UPDATE t_epidatatravel tedt SET
id = (SELECT edt_new_id FROM t_edt_id_map WHERE tedt.id = edt_old_id),
uuid = (SELECT edt_new_uuid FROM t_edt_id_map WHERE tedt.id = edt_old_id),
epidata_id = (SELECT new_id FROM t_id_map WHERE tedt.epidata_id = old_id);

-- TRAVELS END

INSERT INTO epidata (SELECT * FROM t_epidata);
INSERT INTO epidataburial (SELECT * FROM t_epidataburial);
INSERT INTO epidatagathering (SELECT * FROM t_epidatagathering);
INSERT INTO epidatatravel (SELECT * FROM t_epidatatravel);

UPDATE cases SET epidata_id = m.new_id FROM t_id_map m WHERE cases.epidata_id = m.old_id;

-- EPI DATA END

-- SYMPTOMS

DROP TABLE IF EXISTS t_symptoms;
DROP TABLE IF EXISTS t_id_map;

CREATE temp table t_symptoms
AS SELECT s.* FROM symptoms s WHERE s.id IN (SELECT ca.symptoms_id FROM cases ca) AND s.id IN (SELECT vi.symptoms_id FROM visit vi);

CREATE temp table t_id_map
AS SELECT id AS old_id,
nextval('entity_seq') AS new_id,
create_new_uuid(uuid) AS new_uuid
FROM t_symptoms;

UPDATE t_symptoms ts SET
id = (SELECT new_id FROM t_id_map WHERE ts.id = old_id),
uuid = (SELECT new_uuid FROM t_id_map WHERE ts.id = old_id);

INSERT INTO symptoms (SELECT * FROM t_symptoms);

UPDATE cases SET symptoms_id = m.new_id FROM t_id_map m WHERE cases.symptoms_id = m.old_id;

-- SYMPTOMS END

DROP TABLE IF EXISTS t_epidata;
DROP TABLE IF EXISTS t_epidataburial;
DROP TABLE IF EXISTS t_epidatagathering;
DROP TABLE IF EXISTS t_epidatatravel;
DROP TABLE IF EXISTS t_id_map;
DROP TABLE IF EXISTS t_edb_id_map;
DROP TABLE IF EXISTS t_edg_id_map;
DROP TABLE IF EXISTS t_edt_id_map;
DROP TABLE IF EXISTS t_epidataburial_location;
DROP TABLE IF EXISTS t_epidatagathering_location;
DROP TABLE IF EXISTS t_edbl_id_map;
DROP TABLE IF EXISTS t_edgl_id_map;

INSERT INTO schema_version (version_number, comment) VALUES (245, 'Clone symptoms and epi data linked to cases and contacts/visits at the same time #2735');

-- 2020-09-01 - Store the status of the PIA account for a person
ALTER TABLE person ADD COLUMN symptomjournalstatus varchar(255);
ALTER TABLE person_history ADD COLUMN symptomjournalstatus varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (246, 'Add SymptomJournalStatus to allow status exchange with external journals. #1970');

-- 2020-09-03 - Add "Has COVID app" and "COVID Code generated and delivered" fields on person
ALTER TABLE person ADD COLUMN hasCovidApp boolean DEFAULT false;
ALTER TABLE person_history ADD COLUMN hasCovidApp boolean DEFAULT false;

ALTER TABLE person ADD COLUMN covidCodeDelivered boolean DEFAULT false;
ALTER TABLE person_history ADD COLUMN covidCodeDelivered boolean DEFAULT false;

INSERT INTO schema_version (version_number, comment) VALUES (247, 'SwissCOVID-App fields (for Switzerland and COVID only), #2725');

-- 2020-09-07 - Add reporting user on event participant
ALTER TABLE eventparticipant ADD COLUMN reportingUser_id bigint;
ALTER TABLE eventparticipant ADD CONSTRAINT fk_eventparticipant_reportingUser_id FOREIGN KEY (reportingUser_id) REFERENCES users(id);

ALTER TABLE eventparticipant_history ADD COLUMN reportingUser_id bigint;
ALTER TABLE eventparticipant_history ADD CONSTRAINT fk_eventparticipant_history_reportingUser_id FOREIGN KEY (reportingUser_id) REFERENCES users(id);

INSERT INTO schema_version (version_number, comment) VALUES (248, 'Add reporting user on event participant #2789');

-- 2020-09-08 - Add "Official order sent" and corresponding date to cases and contacts #2847
ALTER TABLE cases ADD COLUMN quarantineofficialordersent boolean DEFAULT false;
ALTER TABLE cases ADD COLUMN quarantineofficialordersentdate timestamp;
ALTER TABLE cases_history ADD COLUMN quarantineofficialordersent boolean DEFAULT false;
ALTER TABLE cases_history ADD COLUMN quarantineofficialordersentdate timestamp;

ALTER TABLE contact ADD COLUMN quarantineofficialordersent boolean DEFAULT false;
ALTER TABLE contact ADD COLUMN quarantineofficialordersentdate timestamp;
ALTER TABLE contact_history ADD COLUMN quarantineofficialordersent boolean DEFAULT false;
ALTER TABLE contact_history ADD COLUMN quarantineofficialordersentdate timestamp;

INSERT INTO schema_version (version_number, comment) VALUES (249, 'Add "Official order sent" and corresponding date to cases and contacts #2847');

-- 2020-07-29 Campaign diagram visualisation refinement

-- Hotfix additions to avoid errors for servers older than 2 months
ALTER TABLE campaignformmeta DROP COLUMN IF EXISTS campaignformtranslations;
ALTER TABLE campaignformmeta_history DROP COLUMN IF EXISTS campaignformtranslations;
ALTER TABLE campaignformmeta ADD COLUMN campaignformtranslations json;
-- End of hotfix additions

ALTER TABLE campaignformmeta ALTER COLUMN campaignformelements TYPE json USING campaignformelements::json;
ALTER TABLE campaignformmeta ALTER COLUMN campaignformtranslations TYPE json USING campaignformtranslations::json;
ALTER TABLE campaignformmeta_history ADD COLUMN campaignformelements json;
ALTER TABLE campaignformmeta_history ADD COLUMN campaignformtranslations json;

INSERT INTO schema_version (version_number, comment) VALUES (250, 'Campaign diagram visualization refinement #2753');

-- 2020-09-07 Campaign dashboard element

ALTER TABLE campaigns ADD COLUMN dashboardElements json;
ALTER TABLE campaigns_history ADD COLUMN dashboardElements json;

create or replace function cast_to_int(text, integer) returns integer as $$
begin
    return cast($1 as integer);
exception
    when invalid_text_representation then
        return $2;
end;
$$ language plpgsql immutable;

INSERT INTO schema_version (version_number, comment) VALUES (251, 'Campaign dashboard element #2527');

-- 2020-09-14 Add person_locations table and remove person reference from locations #2746

CREATE TABLE person_locations(
	person_id bigint NOT NULL,
	location_id bigint NOT NULL,
	sys_period tstzrange NOT NULL
);

ALTER TABLE person_locations OWNER TO sormas_user;
ALTER TABLE ONLY person_locations ADD CONSTRAINT unq_person_locations_0 UNIQUE (person_id, location_id);
ALTER TABLE ONLY person_locations ADD CONSTRAINT fk_person_locations_person_id FOREIGN KEY (person_id) REFERENCES person(id);
ALTER TABLE ONLY person_locations ADD CONSTRAINT fk_person_locations_location_id FOREIGN KEY (location_id) REFERENCES location(id);

CREATE TABLE person_locations_history (LIKE person_locations);
CREATE TRIGGER versioning_trigger BEFORE INSERT OR UPDATE OR DELETE ON person_locations
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'person_locations_history', true);
ALTER TABLE person_locations_history OWNER TO sormas_user;

INSERT INTO person_locations (person_id, location_id) SELECT l.person_id, l.id FROM location l WHERE l.person_id IS NOT NULL;

ALTER TABLE location DROP COLUMN person_id;

INSERT INTO schema_version (version_number, comment) VALUES (252, 'Add person_locations table and remove person reference from locations #2746');

-- 2020-09-21 - Store if quarantine period has been reduced #2235
ALTER TABLE cases ADD COLUMN quarantinereduced boolean DEFAULT false;
ALTER TABLE contact ADD COLUMN quarantinereduced boolean DEFAULT false;

ALTER TABLE cases_history ADD COLUMN quarantinereduced boolean DEFAULT false;
ALTER TABLE contact_history ADD COLUMN quarantinereduced boolean DEFAULT false;

INSERT INTO schema_version (version_number, comment) VALUES (253, 'Store if quarantine period has been reduced #2235');

-- 2020-09-21 Add new field externalId as per feature #2670
ALTER TABLE person ADD COLUMN externalid varchar(255);
ALTER TABLE person_history ADD COLUMN externalid varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (254, 'Add new field externalId as per feature #2670');

-- 2020-09-18 Add action title
ALTER TABLE action ADD COLUMN title character varying(512);
ALTER TABLE action_history ADD COLUMN title character varying(512);

INSERT INTO schema_version (version_number, comment) VALUES (255, 'Add action.title');

-- 2020-09-18 Add event title
ALTER TABLE events ADD COLUMN eventTitle character varying(512);
ALTER TABLE events_history ADD COLUMN eventTitle character varying(512);

INSERT INTO schema_version (version_number, comment) VALUES (256, 'Add event.eventTitle');

-- 2020-09-25 Cases > Minimal Essential Data (MED) for Switzerland #2959
ALTER TABLE cases
    ADD COLUMN caseidism integer,
    ADD COLUMN covidtestreason varchar(255),
    ADD COLUMN covidtestreasondetails varchar(512),
    ADD COLUMN contacttracingfirstcontacttype varchar(255),
    ADD COLUMN contacttracingfirstcontactdate timestamp,
    ADD COLUMN quarantinereasonbeforeisolation varchar(255),
    ADD COLUMN quarantinereasonbeforeisolationdetails varchar(512),
    ADD COLUMN endofisolationreason varchar(255),
    ADD COLUMN endofisolationreasondetails varchar(512);

ALTER TABLE cases_history
    ADD COLUMN caseidism integer,
    ADD COLUMN covidtestreason varchar(255),
    ADD COLUMN covidtestreasondetails varchar(512),
    ADD COLUMN contacttracingfirstcontacttype varchar(255),
    ADD COLUMN contacttracingfirstcontactdate timestamp,
    ADD COLUMN quarantinereasonbeforeisolation varchar(255),
    ADD COLUMN quarantinereasonbeforeisolationdetails varchar(512),
    ADD COLUMN endofisolationreason varchar(255),
    ADD COLUMN endofisolationreasondetails varchar(512);

INSERT INTO schema_version (version_number, comment) VALUES (257, 'Cases > Minimal Essential Data (MED) for Switzerland #2959');

-- 2020-09-22 Add facility fields to location and refactor occupation facilities for persons #2456
ALTER TABLE location ADD COLUMN facilitytype varchar(255);
ALTER TABLE location_history ADD COLUMN facilitytype varchar(255);
ALTER TABLE location ADD COLUMN facility_id bigint;
ALTER TABLE location_history ADD COLUMN facility_id bigint;
ALTER TABLE location ADD CONSTRAINT fk_location_facility_id FOREIGN KEY (facility_id) REFERENCES facility(id);
ALTER TABLE location ADD COLUMN facilitydetails varchar(512);
ALTER TABLE location_history ADD COLUMN facilitydetails varchar(512);

CREATE temp table t_id_map
AS SELECT id AS person_id, nextval('entity_seq') AS location_id, create_new_uuid(uuid) AS uuid, occupationregion_id, occupationdistrict_id, occupationcommunity_id, occupationfacility_id, occupationfacilitydetails, occupationfacilitytype
FROM person WHERE occupationregion_id IS NOT NULL OR occupationdistrict_id IS NOT NULL OR occupationcommunity_id IS NOT NULL OR occupationfacility_id IS NOT NULL;

INSERT INTO location (id, uuid, changedate, creationdate, region_id, district_id, community_id, facility_id, facilitydetails, facilitytype, addresstype)
SELECT location_id, uuid, now(), now(), occupationregion_id, occupationdistrict_id, occupationcommunity_id, occupationfacility_id, occupationfacilitydetails, occupationfacilitytype, 'PLACE_OF_WORK'
FROM t_id_map;
INSERT INTO person_locations (person_id, location_id) SELECT person_id, location_id FROM t_id_map;
ALTER TABLE person DROP COLUMN occupationregion_id, DROP COLUMN occupationdistrict_id, DROP COLUMN occupationcommunity_id, DROP COLUMN occupationfacilitytype, DROP COLUMN occupationfacility_id, DROP COLUMN occupationfacilitydetails;

INSERT INTO schema_version (version_number, comment) VALUES (258, 'Add facility fields to location and refactor occupation facilities for persons #2456');

-- 202-10-01 Split general signs of disease #2916
ALTER TABLE symptoms ADD COLUMN shivering character varying(255);
ALTER TABLE symptoms RENAME generalsignsofdisease to feelingill;

ALTER TABLE symptoms_history ADD COLUMN shivering character varying(255);
ALTER TABLE symptoms_history RENAME generalsignsofdisease to feelingill;


INSERT INTO schema_version (version_number, comment) VALUES (259, 'Split general signs of disease #2916');

-- 2020-10-01 Contacts > Minimal Essential Data (MED) for Switzerland #2960
ALTER TABLE contact
    ADD COLUMN endofquarantinereason varchar(255),
    ADD COLUMN endofquarantinereasondetails varchar(512);

ALTER TABLE contact_history
    ADD COLUMN endofquarantinereason varchar(255),
    ADD COLUMN endofquarantinereasondetails varchar(512);

INSERT INTO schema_version (version_number, comment) VALUES (260, 'Contacts > Minimal Essential Data (MED) for Switzerland #2960');

-- 2020-09-16 Add total series to campaigndiagramdefinition to calculate percentage values #2528
ALTER TABLE campaigndiagramdefinition ADD COLUMN campaignseriestotal json;
ALTER TABLE campaigndiagramdefinition_history ADD COLUMN campaignseriestotal json;

INSERT INTO schema_version (version_number, comment) VALUES (261, 'Add series total to campaigndiagramdefinition to calculate percentage values #2528');

-- 2020-10-01 Add possibility to set percentage visualization as default for campaign diagram definitions #2528
ALTER TABLE campaigndiagramdefinition ADD COLUMN percentagedefault boolean DEFAULT false;
ALTER TABLE campaigndiagramdefinition_history ADD COLUMN percentagedefault boolean DEFAULT false;

INSERT INTO schema_version (version_number, comment) VALUES (262, 'Add possibility to set percentage visualization as default for campaign diagram definitions #2528');

-- 2020-10-02 Add new field returningTraveler to contact
ALTER TABLE contact ADD COLUMN returningtraveler varchar(255);
ALTER TABLE contact_history ADD COLUMN returningtraveler varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (263, 'Add new field returningTraveler to contact #2603');
-- 2020-08-13 Sormas 2 Sormas sharing information #2624
CREATE TABLE sormastosormasorigininfo (
    id bigint NOT NULL,
    uuid varchar(36) not null unique,
    creationdate timestamp without time zone NOT NULL,
    changedate timestamp not null,
    organizationid varchar(512),
    sendername varchar(512),
    senderemail varchar(512),
    senderphonenumber varchar(512),
    ownershiphandedover boolean NOT NULL DEFAULT false,
    comment varchar(4096),
    primary key(id)
);
ALTER TABLE sormastosormasorigininfo OWNER TO sormas_user;

ALTER TABLE cases ADD COLUMN sormasToSormasOriginInfo_id bigint;
ALTER TABLE cases ADD CONSTRAINT fk_cases_sormasToSormasOriginInfo_id FOREIGN KEY (sormasToSormasOriginInfo_id) REFERENCES sormastosormasorigininfo (id) ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE contact ADD COLUMN sormasToSormasOriginInfo_id bigint;
ALTER TABLE contact ADD CONSTRAINT fk_contact_sormasToSormasOriginInfo_id FOREIGN KEY (sormasToSormasOriginInfo_id) REFERENCES sormastosormasorigininfo (id) ON UPDATE NO ACTION ON DELETE NO ACTION;

CREATE TABLE sormastosormasshareinfo (
    id bigint NOT NULL,
    uuid varchar(36) not null unique,
    creationdate timestamp without time zone NOT NULL,
    changedate timestamp not null,
    caze_id bigint,
    contact_id bigint,
    organizationid varchar(512),
    sender_id bigint,
    ownershiphandedover boolean NOT NULL DEFAULT false,
    comment varchar(4096),
    primary key(id)
);

ALTER TABLE sormastosormasshareinfo OWNER TO sormas_user;
ALTER TABLE sormastosormasshareinfo ADD CONSTRAINT fk_sormastosormasshareinfo_caze_id FOREIGN KEY (caze_id) REFERENCES cases (id) ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE sormastosormasshareinfo ADD CONSTRAINT fk_sormastosormasshareinfo_contact_id FOREIGN KEY (contact_id) REFERENCES contact (id) ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE sormastosormasshareinfo ADD CONSTRAINT fk_sormastosormasshareinfo_sender_id FOREIGN KEY (sender_id) REFERENCES users (id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO schema_version (version_number, comment) VALUES (264, 'Store Sormas 2 Sormas sharing information #2624');

-- 2020-10-05 Add new field: Quarantine before isolation #2977
ALTER TABLE cases
    ADD COLUMN wasInQuarantineBeforeIsolation varchar(255);

ALTER TABLE cases_history
    ADD COLUMN wasInQuarantineBeforeIsolation varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (265, 'Add new field: Quarantine before isolation #2977');
-- 2020-09-23 CampaignFormMeta to Campaigns relation #2855

CREATE TABLE campaign_campaignformmeta(
                                campaign_id bigint NOT NULL,
                                campaignformmeta_id bigint NOT NULL,
                                sys_period tstzrange NOT NULL
);

ALTER TABLE campaign_campaignformmeta OWNER TO sormas_user;
ALTER TABLE ONLY campaign_campaignformmeta ADD CONSTRAINT unq_campaign_campaignformmeta_0 UNIQUE (campaign_id, campaignformmeta_id);
ALTER TABLE ONLY campaign_campaignformmeta ADD CONSTRAINT fk_campaign_campaignformmeta_campaign_id FOREIGN KEY (campaign_id) REFERENCES campaigns(id);
ALTER TABLE ONLY campaign_campaignformmeta ADD CONSTRAINT fk_campaign_campaignformmeta_meta_id FOREIGN KEY (campaignformmeta_id) REFERENCES campaignformmeta(id);

CREATE TABLE campaign_campaignformmeta_history (LIKE campaign_campaignformmeta);
CREATE TRIGGER versioning_trigger BEFORE INSERT OR UPDATE OR DELETE ON campaign_campaignformmeta
    FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'campaign_campaignformmeta_history', true);
ALTER TABLE campaign_campaignformmeta_history OWNER TO sormas_user;

INSERT INTO schema_version (version_number, comment) VALUES (266, 'CampaignFormMeta to Campaigns relation #2855');

--2020-10-09 Add boolean to users to active window GDPR
ALTER TABLE users ADD COLUMN hasConsentedToGdpr boolean default false;
ALTER TABLE users_history ADD COLUMN hasConsentedToGdpr boolean default false;
INSERT INTO schema_version (version_number, comment) VALUES (267, 'Add gdpr popup to user');

--2020-10-22 Optimize person similarity/duplication check
CREATE INDEX similarity_index
    ON person using gist ((firstName || ' ' || lastName) gist_trgm_ops);
INSERT INTO schema_version (version_number, comment) VALUES (268, 'Optimize person similarity/duplication check');

-- 2020-10-27 - Store visit source #2083
ALTER TABLE visit ADD COLUMN origin varchar(255);
ALTER TABLE visit_history ADD COLUMN origin varchar(255);
UPDATE visit SET origin='USER';

INSERT INTO schema_version (version_number, comment) VALUES (269, 'Add new field origin to visits as per feature #2083');
-- 2020-10-22 Sormas 2 Sormas samples #3210
ALTER TABLE samples ADD COLUMN sormasToSormasOriginInfo_id bigint;
ALTER TABLE samples ADD CONSTRAINT fk_samples_sormasToSormasOriginInfo_id FOREIGN KEY (sormasToSormasOriginInfo_id) REFERENCES sormastosormasorigininfo (id) ON UPDATE NO ACTION ON DELETE NO ACTION;


ALTER TABLE sormastosormasshareinfo ADD COLUMN sample_id bigint;
ALTER TABLE sormastosormasshareinfo ADD CONSTRAINT fk_sormastosormasshareinfo_sample_id FOREIGN KEY (sample_id) REFERENCES samples (id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO schema_version (version_number, comment) VALUES (270, 'Sormas 2 Sormas samples #3210');

-- 2020-10-12 Add event investigation status
ALTER TABLE events ADD COLUMN eventInvestigationStatus varchar(255);
ALTER TABLE events_history ADD COLUMN eventInvestigationStatus varchar(255);
ALTER TABLE events ADD COLUMN eventInvestigationStartDate timestamp;
ALTER TABLE events_history ADD COLUMN eventInvestigationStartDate timestamp;
ALTER TABLE events ADD COLUMN eventInvestigationEndDate timestamp;
ALTER TABLE events_history ADD COLUMN eventInvestigationEndDate timestamp;

INSERT INTO schema_version (version_number, comment) VALUES (271, 'Add event.eventInvestigationStatus #2992');

-- 2020-10-30 Increase case directory performance #3137
ALTER TABLE visit DROP CONSTRAINT IF EXISTS fk_visit_caze_id;
ALTER TABLE visit ADD CONSTRAINT fk_visit_caze_id FOREIGN KEY (caze_id) REFERENCES cases (id);
CREATE INDEX IF NOT EXISTS idx_visit_caze_id ON visit USING HASH (caze_id);
CREATE INDEX IF NOT EXISTS idx_eventparticipant_resultingcase_id ON eventparticipant USING hash (resultingcase_id);

INSERT INTO schema_version (version_number, comment) VALUES (272, 'Increase case directory performance #3137');

-- 2020-11-02 Drop not null constraint from event description #3223
ALTER TABLE events ALTER COLUMN eventdesc DROP NOT NULL;

INSERT INTO schema_version (version_number, comment) VALUES (273, 'Drop not null constraint from event description #3223');

-- 2020-11-05 Drop not null constraint from event history description #3391
ALTER TABLE events_history ALTER COLUMN eventdesc DROP NOT NULL;

INSERT INTO schema_version (version_number, comment) VALUES (274, 'Drop not null constraint from event history description #3391');

-- 2020-11-06 Split follow-up duration #3100
ALTER TABLE diseaseconfiguration ADD COLUMN casefollowupduration integer;
ALTER TABLE diseaseconfiguration ADD COLUMN eventparticipantfollowupduration integer;
ALTER TABLE diseaseconfiguration_history ADD COLUMN casefollowupduration integer;
ALTER TABLE diseaseconfiguration_history ADD COLUMN eventparticipantfollowupduration integer;
UPDATE diseaseconfiguration SET casefollowupduration = followupduration;
UPDATE diseaseconfiguration SET eventparticipantfollowupduration = followupduration;

INSERT INTO schema_version (version_number, comment) VALUES (275, 'Split follow-up duration #3100');

-- 2020-??-?? Create country table #2993
CREATE TABLE country (
    id bigint NOT NULL,
    uuid varchar(36) not null unique,
    creationdate timestamp without time zone NOT NULL,
    changedate timestamp not null,
    archived boolean not null default false,
    defaultname varchar(255),
    externalid varchar(255),
    isocode varchar(3) unique not null,
    unocode varchar(3) unique,
    primary key(id)
);
ALTER TABLE country OWNER TO sormas_user;

INSERT INTO schema_version (version_number, comment) VALUES (276, 'Create country table #2993');

-- 2020-11-10 Add documents

CREATE TABLE documents (
    id bigint PRIMARY KEY NOT NULL,
    uuid character varying(36) NOT NULL,
    changedate timestamp without time zone NOT NULL,
    creationdate timestamp without time zone NOT NULL,
    deleted boolean DEFAULT false,
    uploadinguser_id bigint NOT NULL,
    name character varying(255) NOT NULL,
    mimetype character varying(255) NOT NULL,
    size bigint NOT NULL,
    storage_reference character varying(255) NOT NULL,
    relatedentity_uuid character varying(36) NOT NULL,
    relatedentity_type character varying(255) NOT NULL,

    CONSTRAINT fk_documents_uploadinguser_id FOREIGN KEY (uploadinguser_id) REFERENCES users(id)
);
ALTER TABLE documents OWNER TO sormas_user;

INSERT INTO schema_version (version_number, comment) VALUES (277, 'Add documents #2328');

-- 2020-11-06 Extend event participant jurisdiction calculation #2902
ALTER TABLE eventparticipant ADD COLUMN region_id bigint;
ALTER TABLE eventparticipant ADD COLUMN district_id bigint;
ALTER TABLE eventparticipant ADD CONSTRAINT fk_eventparticipant_region_id FOREIGN KEY (region_id) REFERENCES region (id);
ALTER TABLE eventparticipant ADD CONSTRAINT fk_eventparticipant_district_id FOREIGN KEY (district_id) REFERENCES district (id);
INSERT INTO schema_version (version_number, comment) VALUES (278, 'Extend event participant jurisdiction calculation #2902');

-- 2020-10-15 New exposure entity and migration #2948
ALTER TABLE epidata ADD COLUMN exposuredetailsknown varchar(255);
ALTER TABLE epidata_history ADD COLUMN exposuredetailsknown varchar(255);

UPDATE epidata SET exposuredetailsknown =
CASE
WHEN traveled = 'YES' OR gatheringattended = 'YES' OR burialattended = 'YES' THEN 'YES'
WHEN traveled = 'NO' OR gatheringattended = 'NO' OR burialattended = 'NO' THEN 'NO'
WHEN traveled = 'UNKNOWN' OR gatheringattended = 'UNKNOWN' OR burialattended = 'UNKNOWN' THEN 'UNKNOWN'
END;

CREATE TABLE exposures(
    id bigint not null,
    uuid varchar(36) not null unique,
    changedate timestamp not null,
    creationdate timestamp not null,
    epidata_id bigint not null,
    reportinguser_id bigint,
    startdate timestamp,
    enddate timestamp,
    description text,
    exposuretype varchar(255) not null,
    exposuretypedetails text,
    location_id bigint not null,
    typeofplace varchar(255),
    typeofplacedetails text,
    meansoftransport varchar(255),
    meansoftransportdetails text,
    connectionnumber varchar(512),
    seatnumber varchar(512),
    indoors varchar(255),
    outdoors varchar(255),
    wearingmask varchar(255),
    wearingppe varchar(255),
    otherprotectivemeasures varchar(255),
    protectivemeasuresdetails text,
    shortdistance varchar(255),
    longfacetofacecontact varchar(255),
    animalmarket varchar(255),
    percutaneous varchar(255),
    contacttobodyfluids varchar(255),
    handlingsamples varchar(255),
    eatingrawanimalproducts varchar(255),
    handlinganimals varchar(255),
    animalcondition varchar(255),
    animalvaccinated varchar(255),
    animalcontacttype varchar(255),
    animalcontacttypedetails text,
    bodyofwater varchar(255),
    watersource varchar(255),
    watersourcedetails text,
    contacttocase_id bigint,
    gatheringtype varchar(255),
    gatheringdetails text,
    habitationtype varchar(255),
    habitationdetails text,
    typeofanimal varchar(255),
    typeofanimaldetails text,
    physicalcontactduringpreparation varchar(255),
    physicalcontactwithbody varchar(255),
    deceasedpersonill varchar(255),
    deceasedpersonname varchar(512),
    deceasedpersonrelation varchar(512),
    prophylaxis varchar(255),
    prophylaxisdate timestamp,
    riskarea varchar(255),
    sys_period tstzrange not null,
    primary key(id)
);

ALTER TABLE exposures OWNER TO sormas_user;
ALTER TABLE exposures ADD CONSTRAINT fk_exposures_epidata_id FOREIGN KEY (epidata_id) REFERENCES epidata(id);
ALTER TABLE exposures ADD CONSTRAINT fk_exposures_reportinguser_id FOREIGN KEY (reportinguser_id) REFERENCES users(id);
ALTER TABLE exposures ADD CONSTRAINT fk_exposures_location_id FOREIGN KEY (location_id) REFERENCES location(id);
ALTER TABLE exposures ADD CONSTRAINT fk_exposures_contacttocase_id FOREIGN KEY (contacttocase_id) REFERENCES contact(id);

CREATE TABLE exposures_history (LIKE exposures);
CREATE TRIGGER versioning_trigger BEFORE INSERT OR UPDATE OR DELETE ON exposures
    FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'exposures_history', true);
ALTER TABLE exposures_history OWNER TO sormas_user;

INSERT INTO exposures(
    id, uuid, changedate, creationdate, epidata_id, location_id, deceasedpersonname, deceasedpersonrelation, physicalcontactwithbody,
    deceasedpersonill, startdate, enddate, exposuretype)
SELECT nextval('entity_seq'),
       overlay(overlay(overlay(
            substring(upper(REPLACE(CAST(CAST(md5(CAST(random() AS text) || CAST(clock_timestamp() AS text)) AS uuid) AS text), '-', '')), 0, 30)
            placing '-' from 7) placing '-' from 14) placing '-' from 21),
       now(), now(), b.epidata_id, b.burialaddress_id, b.burialpersonname, b.burialrelation, b.burialtouching,
       b.burialill, b.burialdatefrom, b.burialdateto, 'BURIAL'
FROM epidataburial b;

INSERT INTO exposures(
    id, uuid, changedate, creationdate, epidata_id, location_id, startdate, enddate, description, exposuretype)
SELECT nextval('entity_seq'),
       overlay(overlay(overlay(
                               substring(upper(REPLACE(CAST(CAST(md5(CAST(random() AS text) || CAST(clock_timestamp() AS text)) AS uuid) AS text), '-', '')), 0, 30)
                               placing '-' from 7) placing '-' from 14) placing '-' from 21),
       now(), now(), g.epidata_id, g.gatheringaddress_id, g.gatheringdate, g.gatheringdate, g.description, 'GATHERING'
FROM epidatagathering g;

DROP TABLE IF EXISTS tl_map;

CREATE temp table tl_map
AS SELECT id AS travel_id,
          nextval('entity_seq') AS location_id,
          create_new_uuid(uuid) AS location_uuid,
          epidata_id,
          traveldatefrom,
          traveldateto,
          concat_ws(', ', regexp_replace(traveltype, '_', ' '), traveldestination) AS traveldetails
   FROM epidatatravel;

INSERT INTO location (id, uuid, changedate, creationdate, details)
SELECT location_id, location_uuid, now(), now(), traveldetails
FROM tl_map;

INSERT INTO exposures(
    id, uuid, changedate, creationdate, epidata_id, location_id, startdate, enddate, exposuretype)
SELECT nextval('entity_seq'),
       overlay(overlay(overlay(
                               substring(upper(REPLACE(CAST(CAST(md5(CAST(random() AS text) || CAST(clock_timestamp() AS text)) AS uuid) AS text), '-', '')), 0, 30)
                               placing '-' from 7) placing '-' from 14) placing '-' from 21),
       now(), now(), tl.epidata_id, tl.location_id, tl.traveldatefrom, tl.traveldateto, 'TRAVEL'
FROM tl_map tl;

DROP TABLE IF EXISTS tl_map;

INSERT INTO schema_version (version_number, comment) VALUES (279, 'New exposure entity and migration #2948');

-- 2020-10-21 Epi data migration #2949
ALTER TABLE epidata ADD COLUMN contactwithsourcecaseknown varchar(255);
ALTER TABLE epidata ADD COLUMN hightransmissionriskarea varchar(255);
ALTER TABLE epidata ADD COLUMN largeoutbreaksarea varchar(255);
ALTER TABLE epidata_history ADD COLUMN contactwithsourcecaseknown varchar(255);
ALTER TABLE epidata_history ADD COLUMN hightransmissionriskarea varchar(255);
ALTER TABLE epidata_history ADD COLUMN largeoutbreaksarea varchar(255);

CREATE OR REPLACE FUNCTION migrate_epidata(epidata_field_name text, exposures_field_name text, exposures_field_value text, exposuretype text,
epidata_startdate_field_name text default 'null', epidata_enddate_field_name text default 'null', epidata_description_field_name text default 'null',
epidata_locationinfo_field_name text default 'null')
RETURNS VOID
LANGUAGE plpgsql
SECURITY DEFINER
AS
$BODY$
BEGIN
EXECUTE
    'DROP TABLE IF EXISTS id_map;
    CREATE TEMP TABLE id_map AS
    SELECT id as epidata_id,
           nextval(''entity_seq'') as location_id,
           nextval(''entity_seq'') as exposure_id,
           overlay(overlay(overlay(
               substring(upper(REPLACE(CAST(CAST(md5(CAST(random() AS text) || CAST(clock_timestamp() AS text)) AS uuid) AS text), ''-'', '''')), 0, 30)
               placing ''-'' from 7) placing ''-'' from 14) placing ''-'' from 21) as location_uuid,
           overlay(overlay(overlay(
               substring(upper(REPLACE(CAST(CAST(md5(CAST(random() AS text) || CAST(clock_timestamp() AS text)) AS uuid) AS text), ''-'', '''')), 0, 30)
               placing ''-'' from 7) placing ''-'' from 14) placing ''-'' from 21) as exposure_uuid,
           CAST (' || epidata_startdate_field_name || ' AS timestamp) as startdate,
           CAST (' || epidata_enddate_field_name || ' AS timestamp) as enddate,
           ' || epidata_description_field_name || ' as description,
           ' || epidata_locationinfo_field_name || ' as locationinfo
    FROM epidata WHERE ' || epidata_field_name || ' = ''YES'';

    INSERT INTO location (id, uuid, changedate, creationdate, details)
    SELECT location_id, location_uuid, now(), now(), locationinfo
    FROM id_map;

    INSERT INTO exposures(id, uuid, changedate, creationdate, epidata_id, location_id, exposuretype, ' || exposures_field_name || ', startdate, enddate, description)
    SELECT exposure_id, exposure_uuid, now(), now(), epidata_id, location_id, ' || quote_literal(exposuretype) || ', ' || quote_literal(exposures_field_value) || ',
    startdate, enddate, description FROM id_map;';
END;
$BODY$;

ALTER FUNCTION migrate_epidata(text, text, text, text, text, text, text, text) OWNER TO sormas_user;

UPDATE epidata SET areainfectedanimals = 'YES', eatingrawanimals = 'YES' WHERE eatingrawanimalsininfectedarea = 'YES';

DO $$ BEGIN
PERFORM migrate_epidata('processingconfirmedcasefluidunsafe', 'handlingsamples', 'YES', 'WORK');
PERFORM migrate_epidata('percutaneouscaseblood', 'percutaneous', 'YES', 'WORK');
PERFORM migrate_epidata('directcontactdeadunsafe', 'physicalcontactwithbody', 'YES', 'BURIAL');
PERFORM migrate_epidata('processingsuspectedcasesampleunsafe', 'handlingsamples', 'YES', 'WORK');
PERFORM migrate_epidata('sickdeadanimals', 'animalcondition', 'DEAD', 'ANIMAL_CONTACT', 'sickdeadanimalsdate', 'sickdeadanimalsdate', 'sickdeadanimalsdetails', 'sickdeadanimalslocation');
PERFORM migrate_epidata('eatingrawanimals', 'eatingrawanimalproducts', 'YES', 'ANIMAL_CONTACT', 'null', 'null', 'eatingrawanimalsdetails', 'null');
PERFORM migrate_epidata('rodents', 'typeofanimal', 'RODENT', 'ANIMAL_CONTACT');
PERFORM migrate_epidata('bats', 'typeofanimal', 'BAT', 'ANIMAL_CONTACT');
PERFORM migrate_epidata('primates', 'typeofanimal', 'PRIMATE', 'ANIMAL_CONTACT');
PERFORM migrate_epidata('swine', 'typeofanimal', 'SWINE', 'ANIMAL_CONTACT');
PERFORM migrate_epidata('birds', 'typeofanimal', 'POULTRY', 'ANIMAL_CONTACT');
PERFORM migrate_epidata('rabbits', 'typeofanimal', 'RABBIT', 'ANIMAL_CONTACT');
PERFORM migrate_epidata('cattle', 'typeofanimal', 'CATTLE', 'ANIMAL_CONTACT');
PERFORM migrate_epidata('dogs', 'typeofanimal', 'DOG', 'ANIMAL_CONTACT');
PERFORM migrate_epidata('cats', 'typeofanimal', 'CAT', 'ANIMAL_CONTACT');
PERFORM migrate_epidata('canidae', 'typeofanimal', 'CANIDAE', 'ANIMAL_CONTACT');
PERFORM migrate_epidata('camels', 'typeofanimal', 'CAMEL', 'ANIMAL_CONTACT');
PERFORM migrate_epidata('snakes', 'typeofanimal', 'SNAKE', 'ANIMAL_CONTACT');
PERFORM migrate_epidata('tickbite', 'typeofanimal', 'TICK', 'ANIMAL_CONTACT');
PERFORM migrate_epidata('fleabite', 'typeofanimal', 'FLEA', 'ANIMAL_CONTACT');
PERFORM migrate_epidata('otheranimals', 'typeofanimal', 'OTHER', 'ANIMAL_CONTACT');
PERFORM migrate_epidata('waterbody', 'bodyofwater', 'YES', 'OTHER', 'null', 'null', 'waterbodydetails', 'null');
PERFORM migrate_epidata('visitedhealthfacility', 'habitationtype', 'MEDICAL', 'HABITATION');
PERFORM migrate_epidata('visitedanimalmarket', 'animalmarket', 'YES', 'OTHER');
PERFORM migrate_epidata('areaconfirmedcases', 'riskarea', 'YES', 'TRAVEL');
PERFORM migrate_epidata('kindofexposurebite', 'animalcontacttype', 'BITE', 'ANIMAL_CONTACT');
PERFORM migrate_epidata('kindofexposuretouch', 'animalcontacttype', 'TOUCH', 'ANIMAL_CONTACT');
PERFORM migrate_epidata('kindofexposurescratch', 'animalcontacttype', 'SCRATCH', 'ANIMAL_CONTACT');
PERFORM migrate_epidata('kindofexposurelick', 'animalcontacttype', 'LICK', 'ANIMAL_CONTACT');
PERFORM migrate_epidata('kindofexposureother', 'animalcontacttype', 'OTHER', 'ANIMAL_CONTACT');
END $$;

DROP TABLE IF EXISTS id_map;

DROP TABLE IF EXISTS last_exposure_map;
CREATE TEMP TABLE last_exposure_map AS
SELECT id as epidata_id,
       nextval('entity_seq') as location_id,
       nextval('entity_seq') as exposure_id,
       overlay(overlay(overlay(
                               substring(upper(REPLACE(CAST(CAST(md5(CAST(random() AS text) || CAST(clock_timestamp() AS text)) AS uuid) AS text), '-', '')), 0, 30)
                               placing '-' from 7) placing '-' from 14) placing '-' from 21) as location_uuid,
       overlay(overlay(overlay(
                               substring(upper(REPLACE(CAST(CAST(md5(CAST(random() AS text) || CAST(clock_timestamp() AS text)) AS uuid) AS text), '-', '')), 0, 30)
                               placing '-' from 7) placing '-' from 14) placing '-' from 21) as exposure_uuid,
       dateoflastexposure,
       placeoflastexposure,
       animalcondition,
       animalvaccinationstatus,
       prophylaxisstatus,
       dateofprophylaxis
FROM epidata WHERE dateoflastexposure IS NOT NULL OR placeoflastexposure IS NOT NULL OR animalcondition IS NOT NULL OR animalvaccinationstatus IS NOT NULL OR prophylaxisstatus IS NOT NULL OR dateofprophylaxis IS NOT NULL;

INSERT INTO location (id, uuid, changedate, creationdate, details)
SELECT location_id, location_uuid, now(), now(), placeoflastexposure
FROM last_exposure_map;

INSERT INTO exposures(id, uuid, changedate, creationdate, epidata_id, location_id, exposuretype, startdate, enddate, animalcondition, animalvaccinated, prophylaxis, prophylaxisdate, description)
SELECT exposure_id, exposure_uuid, now(), now(), epidata_id, location_id, 'ANIMAL_CONTACT', dateoflastexposure, dateoflastexposure, animalcondition,
       CASE WHEN animalvaccinationstatus = 'VACCINATED' THEN 'YES' WHEN animalvaccinationstatus = 'UNVACCINATED' THEN 'NO' WHEN animalvaccinationstatus = 'UNKNOWN' THEN 'UNKNOWN' END,
       prophylaxisstatus, dateofprophylaxis, 'Automatic epi data migration based on last exposure details. This exposure may be merged with another exposure with the activity type Animal Contact.'
FROM last_exposure_map;

DROP TABLE IF EXISTS last_exposure_map;
CREATE TEMP TABLE empty_travels_map AS
SELECT id as epidata_id,
       nextval('entity_seq') as location_id,
       nextval('entity_seq') as exposure_id,
       overlay(overlay(overlay(
                               substring(upper(REPLACE(CAST(CAST(md5(CAST(random() AS text) || CAST(clock_timestamp() AS text)) AS uuid) AS text), '-', '')), 0, 30)
                               placing '-' from 7) placing '-' from 14) placing '-' from 21) as location_uuid,
       overlay(overlay(overlay(
                               substring(upper(REPLACE(CAST(CAST(md5(CAST(random() AS text) || CAST(clock_timestamp() AS text)) AS uuid) AS text), '-', '')), 0, 30)
                               placing '-' from 7) placing '-' from 14) placing '-' from 21) as exposure_uuid
FROM epidata WHERE traveled = 'YES' AND epidata.id NOT IN (SELECT epidatatravel.epidata_id FROM epidatatravel);

INSERT INTO location (id, uuid, changedate, creationdate)
SELECT location_id, location_uuid, now(), now()
FROM empty_travels_map;

INSERT INTO exposures(id, uuid, changedate, creationdate, epidata_id, location_id, exposuretype)
SELECT exposure_id, exposure_uuid, now(), now(), epidata_id, location_id, 'TRAVEL'
FROM empty_travels_map;

DROP TABLE IF EXISTS empty_travels_map;
CREATE TEMP TABLE empty_gatherings_map AS
SELECT id as epidata_id,
       nextval('entity_seq') as location_id,
       nextval('entity_seq') as exposure_id,
       overlay(overlay(overlay(
                               substring(upper(REPLACE(CAST(CAST(md5(CAST(random() AS text) || CAST(clock_timestamp() AS text)) AS uuid) AS text), '-', '')), 0, 30)
                               placing '-' from 7) placing '-' from 14) placing '-' from 21) as location_uuid,
       overlay(overlay(overlay(
                               substring(upper(REPLACE(CAST(CAST(md5(CAST(random() AS text) || CAST(clock_timestamp() AS text)) AS uuid) AS text), '-', '')), 0, 30)
                               placing '-' from 7) placing '-' from 14) placing '-' from 21) as exposure_uuid
FROM epidata WHERE gatheringattended = 'YES' AND epidata.id NOT IN (SELECT epidatagathering.epidata_id FROM epidatagathering);

INSERT INTO location (id, uuid, changedate, creationdate)
SELECT location_id, location_uuid, now(), now()
FROM empty_gatherings_map;

INSERT INTO exposures(id, uuid, changedate, creationdate, epidata_id, location_id, exposuretype)
SELECT exposure_id, exposure_uuid, now(), now(), epidata_id, location_id, 'GATHERING'
FROM empty_gatherings_map;

DROP TABLE IF EXISTS empty_gatherings_map;
CREATE TEMP TABLE empty_burials_map AS
SELECT id as epidata_id,
       nextval('entity_seq') as location_id,
       nextval('entity_seq') as exposure_id,
       overlay(overlay(overlay(
                               substring(upper(REPLACE(CAST(CAST(md5(CAST(random() AS text) || CAST(clock_timestamp() AS text)) AS uuid) AS text), '-', '')), 0, 30)
                               placing '-' from 7) placing '-' from 14) placing '-' from 21) as location_uuid,
       overlay(overlay(overlay(
                               substring(upper(REPLACE(CAST(CAST(md5(CAST(random() AS text) || CAST(clock_timestamp() AS text)) AS uuid) AS text), '-', '')), 0, 30)
                               placing '-' from 7) placing '-' from 14) placing '-' from 21) as exposure_uuid
FROM epidata WHERE burialattended = 'YES' AND epidata.id NOT IN (SELECT epidataburial.epidata_id FROM epidataburial);

INSERT INTO location (id, uuid, changedate, creationdate)
SELECT location_id, location_uuid, now(), now()
FROM empty_burials_map;

INSERT INTO exposures(id, uuid, changedate, creationdate, epidata_id, location_id, exposuretype)
SELECT exposure_id, exposure_uuid, now(), now(), epidata_id, location_id, 'BURIAL'
FROM empty_burials_map;

DROP TABLE IF EXISTS empty_burials_map;

UPDATE exposures SET typeofanimaldetails = otheranimalsdetails FROM epidata WHERE epidata.id = epidata_id AND typeofanimal = 'OTHER';
UPDATE exposures SET animalcontacttypedetails = kindofexposuredetails FROM epidata WHERE epidata.id = epidata_id AND animalcontacttype = 'OTHER';
UPDATE exposures SET watersource = epidata.watersource, watersourcedetails = epidata.watersourceother FROM epidata WHERE epidata.id = epidata_id AND bodyofwater = 'YES';
UPDATE exposures SET description = 'Automatic epi data migration based on selected kinds of exposure. This exposure may be merged with another exposure of the activity type Animal Contact.' WHERE exposuretype = 'ANIMAL_CONTACT' AND typeofanimal IS NULL;

UPDATE epidata SET contactwithsourcecaseknown = 'YES' WHERE directcontactconfirmedcase = 'YES' OR directcontactprobablecase = 'YES' OR closecontactprobablecase = 'YES' OR contactwithsourcerespiratorycase = 'YES';

-- TODO - Add this to a future version after the migration has been done on a production system;
/*ALTER TABLE epidata DROP COLUMN rodents, DROP COLUMN bats, DROP COLUMN primates, DROP COLUMN swine, DROP COLUMN birds, DROP COLUMN eatingrawanimals, DROP COLUMN sickdeadanimals,
    DROP COLUMN sickdeadanimalsdetails, DROP COLUMN sickdeadanimalsdate, DROP COLUMN sickdeadanimalslocation, DROP COLUMN cattle, DROP COLUMN otheranimals, DROP COLUMN otheranimalsdetails,
    DROP COLUMN watersource, DROP COLUMN watersourceother, DROP COLUMN waterbody, DROP COLUMN waterbodydetails, DROP COLUMN tickbite, DROP COLUMN burialattended, DROP COLUMN gatheringattended,
    DROP COLUMN traveled, DROP COLUMN dateoflastexposure, DROP COLUMN placeoflastexposure, DROP COLUMN animalcondition, DROP COLUMN fleabite, DROP COLUMN directcontactconfirmedcase,
    DROP COLUMN directcontactprobablecase, DROP COLUMN closecontactprobablecase, DROP COLUMN areaconfirmedcases, DROP COLUMN processingconfirmedcasefluidunsafe, DROP COLUMN percutaneouscaseblood,
    DROP COLUMN directcontactdeadunsafe, DROP COLUMN processingsuspectedcasesampleunsafe, DROP COLUMN eatingrawanimalsininfectedarea, DROP COLUMN eatingrawanimalsdetails,
    DROP COLUMN kindofexposurebite, DROP COLUMN kindofexposuretouch, DROP COLUMN kindofexposurescratch, DROP COLUMN kindofexposurelick, DROP COLUMN kindofexposureother,
    DROP COLUMN kindofexposuredetails, DROP COLUMN animalvaccinationstatus, DROP COLUMN dogs, DROP COLUMN cats, DROP COLUMN canidae, DROP COLUMN rabbits, DROP COLUMN prophylaxisstatus,
    DROP COLUMN dateofprophylaxis, DROP COLUMN visitedhealthfacility, DROP COLUMN contactwithsourcerespiratorycase, DROP COLUMN visitedanimalmarket, DROP COLUMN camels, DROP COLUMN snakes;*/
/*ALTER TABLE epidata_history DROP COLUMN rodents, DROP COLUMN bats, DROP COLUMN primates, DROP COLUMN swine, DROP COLUMN birds, DROP COLUMN eatingrawanimals, DROP COLUMN sickdeadanimals,
    DROP COLUMN sickdeadanimalsdetails, DROP COLUMN sickdeadanimalsdate, DROP COLUMN sickdeadanimalslocation, DROP COLUMN cattle, DROP COLUMN otheranimals, DROP COLUMN otheranimalsdetails,
    DROP COLUMN watersource, DROP COLUMN watersourceother, DROP COLUMN waterbody, DROP COLUMN waterbodydetails, DROP COLUMN tickbite, DROP COLUMN burialattended, DROP COLUMN gatheringattended,
    DROP COLUMN traveled, DROP COLUMN dateoflastexposure, DROP COLUMN placeoflastexposure, DROP COLUMN animalcondition, DROP COLUMN fleabite, DROP COLUMN directcontactconfirmedcase,
    DROP COLUMN directcontactprobablecase, DROP COLUMN closecontactprobablecase, DROP COLUMN areaconfirmedcases, DROP COLUMN processingconfirmedcasefluidunsafe, DROP COLUMN percutaneouscaseblood,
    DROP COLUMN directcontactdeadunsafe, DROP COLUMN processingsuspectedcasesampleunsafe, DROP COLUMN eatingrawanimalsininfectedarea, DROP COLUMN eatingrawanimalsdetails,
    DROP COLUMN kindofexposurebite, DROP COLUMN kindofexposuretouch, DROP COLUMN kindofexposurescratch, DROP COLUMN kindofexposurelick, DROP COLUMN kindofexposureother,
    DROP COLUMN kindofexposuredetails, DROP COLUMN animalvaccinationstatus, DROP COLUMN dogs, DROP COLUMN cats, DROP COLUMN canidae, DROP COLUMN rabbits, DROP COLUMN prophylaxisstatus,
    DROP COLUMN dateofprophylaxis, DROP COLUMN visitedhealthfacility, DROP COLUMN contactwithsourcerespiratorycase, DROP COLUMN visitedanimalmarket, DROP COLUMN camels, DROP COLUMN snakes;

DROP TABLE epidataburial;
DROP TABLE epidatagathering;
DROP TABLE epidatatravel;
DROP TABLE epidataburial_history;
DROP TABLE epidatagathering_history;
DROP TABLE epidatatravel_history;*/

UPDATE epidata SET exposuredetailsknown = 'YES' FROM exposures WHERE (exposuredetailsknown IS NULL OR exposuredetailsknown != 'YES') AND exposures.epidata_id = epidata.id;

UPDATE epidata SET changedate = now();

INSERT INTO schema_version (version_number, comment) VALUES (280, 'Epi data migration #2949');

-- 2020-10-21 Set contact with source case known for all existing cases #2946
UPDATE epidata SET contactwithsourcecaseknown = 'YES' FROM cases WHERE cases.epidata_id = epidata.id AND exists (SELECT 1 FROM contact WHERE contact.resultingcase_id = cases.id);

INSERT INTO schema_version (version_number, comment) VALUES (281, 'Set contact with source case known for all existing cases #2946');

-- 2020-11-18 Add date of first contact #3408
ALTER TABLE contact ADD column multidaycontact boolean default false;
ALTER TABLE contact ADD column firstcontactdate timestamp;

INSERT INTO schema_version (version_number, comment) VALUES (282, 'Add date of first contact #3408');

ALTER TABLE person ADD COLUMN armedforcesrelationtype varchar(255);
ALTER TABLE person_history ADD COLUMN armedforcesrelationtype varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (283, 'Add column armedforcesrelationtype #3418');

-- 2020-11-27 SurvNet Adaptations - Create new field “nosocomial outbreak” to cases #3416
ALTER TABLE cases
    ADD COLUMN nosocomialOutbreak boolean default false,
    ADD COLUMN infectionSetting varchar(255);

ALTER TABLE cases_history
    ADD COLUMN nosocomialoutbreak boolean default false,
    ADD COLUMN infectionsetting varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (284, 'SurvNet Adaptations - Create new field “nosocomial outbreak” to cases #3416');

-- 2020-12-03 SurvNet Adaptations - Create new field “name of guardians” for persons #3413
ALTER TABLE person
    ADD COLUMN namesofotherguardians varchar(512);

ALTER TABLE person_history
    ADD COLUMN namesofotherguardians varchar(512);

INSERT INTO schema_version (version_number, comment) VALUES (285, 'SurvNet Adaptations - Create new field “name of guardians” for persons #3413');

-- 2020-12-08 SurvNet Adaptations - Add multi day contat to contact history #3408

ALTER TABLE contact_history ADD column multidaycontact boolean;
ALTER TABLE contact_history ADD column firstcontactdate timestamp;

INSERT INTO schema_version (version_number, comment) VALUES (286, 'Add date of first contact for contact history #3408');

-- 2020-11-25 SurvNet Adaptations - Create new field “Prohibition to work” for case and contact #3409
ALTER TABLE cases
    ADD COLUMN prohibitiontowork varchar(255),
    ADD COLUMN prohibitiontoworkfrom timestamp,
    ADD COLUMN prohibitiontoworkuntil timestamp;

ALTER TABLE cases_history
    ADD COLUMN prohibitiontowork varchar(255),
    ADD COLUMN prohibitiontoworkfrom timestamp,
    ADD COLUMN prohibitiontoworkuntil timestamp;

ALTER TABLE contact
    ADD COLUMN prohibitiontowork varchar(255),
    ADD COLUMN prohibitiontoworkfrom timestamp,
    ADD COLUMN prohibitiontoworkuntil timestamp;

ALTER TABLE contact_history
    ADD COLUMN prohibitiontowork varchar(255),
    ADD COLUMN prohibitiontoworkfrom timestamp,
    ADD COLUMN prohibitiontoworkuntil timestamp;

INSERT INTO schema_version (version_number, comment) VALUES (287, 'Create new field “Prohibition to work” for case and contact #3409');

-- 2020-11-27 Add institutional partner option to events source type #3207
ALTER TABLE events ADD COLUMN srcInstitutionalPartnerType varchar(255);
ALTER TABLE events_history ADD COLUMN srcInstitutionalPartnerType varchar(255);
ALTER TABLE events ADD COLUMN srcInstitutionalPartnerTypeDetails varchar(512);
ALTER TABLE events_history ADD COLUMN srcInstitutionalPartnerTypeDetails varchar(512);

INSERT INTO schema_version (version_number, comment) VALUES (288, 'Add institutional partner option to events source type #3207');

-- 2020-11-30 Add riskLevel to events with cluster status #3271
ALTER TABLE events ADD column risklevel varchar(255);
ALTER TABLE events_history ADD column risklevel varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (289, 'Add riskLevel to events with cluster status #3271');

-- 2020-11-17 Manually send SMS #3253
CREATE TABLE manualmessagelog
(
    id             bigint                      NOT NULL,
    changedate     timestamp without time zone NOT NULL,
    creationdate   timestamp without time zone NOT NULL,
    uuid           character varying(36)       NOT NULL,
    messagetype    character varying(255)      NOT NULL,
    sentdate       timestamp                   NOT NULL,
    sendinguser_id bigint                      NOT NULL,
    recipientperson_id bigint                  NOT NULL,
    PRIMARY KEY (id)
);

ALTER TABLE manualmessagelog OWNER TO sormas_user;
ALTER TABLE manualmessagelog ADD CONSTRAINT fk_manualmessagelog_sendinguser_id FOREIGN KEY (sendinguser_id) REFERENCES users(id);
ALTER TABLE manualmessagelog ADD CONSTRAINT fk_manualmessagelog_recipientperson_id FOREIGN KEY (recipientperson_id) REFERENCES person(id);

INSERT INTO schema_version (version_number, comment) VALUES (290, 'Manually send SMS #3253');

-- 2020-12-07 Add LabMessage #3486
CREATE TABLE labmessage (
        id bigint not null,
        uuid varchar(36) not null unique,
        changedate timestamp not null,
        creationdate timestamp not null,
        sampledatetime timestamp,
        samplereceiveddate timestamp,
        labsampleid text,
        samplematerial varchar(255),
        testlabname varchar(255),
        testlabexternalid varchar(255),
        testlabpostalcode varchar(255),
        testlabcity varchar(255),
        specimencondition varchar(255),
        testtype varchar(255),
        testeddisease varchar(255),
        testdatetime timestamp,
        testresult varchar(255),
        personfirstName varchar(255),
        personlastName varchar(255),
        personsex varchar(255),
        personbirthdatedd integer,
        personbirthdatemm integer,
        personbirthdateyyyy integer,
        personpostalcode varchar(255),
        personcity varchar(255),
        personstreet varchar(255),
        personhousenumber varchar(255),
        labMessageDetails text,
        processed boolean default false,
        sys_period tstzrange not null,
        primary key(id)
);

CREATE TABLE labmessage_history (LIKE labmessage);

INSERT INTO schema_version (version_number, comment) VALUES (291, 'Add LabMessage #3486');

-- 2020-12-11 Create contacts-visits index #3673
CREATE INDEX IF NOT EXISTS idx_contacts_visits_contact_id ON contacts_visits USING HASH (contact_id);

INSERT INTO schema_version (version_number, comment) VALUES (292, 'Create contacts-visits index #3673');

-- SurvNet Adaptations - Create new field “Salutation” for persons #3411
ALTER TABLE person
    ADD COLUMN salutation varchar(255),
    ADD COLUMN othersalutation text;

ALTER TABLE person_history
    ADD COLUMN salutation varchar(255),
    ADD COLUMN othersalutation text;

INSERT INTO schema_version (version_number, comment) VALUES (293, 'SurvNet Adaptations - Create new field “Salutation” for persons #3411');

-- 2020-12-11 - Add patient exposition role to exposures #3407
ALTER TABLE exposures ADD COLUMN patientexpositionrole varchar(255);
ALTER TABLE exposures_history ADD COLUMN patientexpositionrole varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (294, 'Add patient exposition role to exposures #3407');

-- 2020-12-09 SurvNet Adaptations - Create new fields “Country of birth” and “nationality” for persons #3412
ALTER TABLE person
    ADD COLUMN birthname varchar(512),
    ADD COLUMN birthcountry_id bigint,
    ADD COLUMN citizenship_id bigint,
    ADD CONSTRAINT fk_person_placeofbirthcountry_id FOREIGN KEY (birthcountry_id) REFERENCES country (id),
    ADD CONSTRAINT fk_person_nationality_id FOREIGN KEY (citizenship_id) REFERENCES country (id);

ALTER TABLE person_history
    ADD COLUMN birthname varchar(512),
    ADD COLUMN birthcountry_id bigint,
    ADD COLUMN citizenship_id bigint,
    ADD CONSTRAINT fk_person_birthcountry_id FOREIGN KEY (birthcountry_id) REFERENCES country (id),
    ADD CONSTRAINT fk_person_citizenship_id FOREIGN KEY (citizenship_id) REFERENCES country (id);

INSERT INTO schema_version (version_number, comment) VALUES (295, 'SurvNet Adaptations - Create new fields “Country of birth” and “nationality” for persons #3412');

-- 2020-12-14 Change namesOfOtherGuardians to namesOfGuardians #3413
ALTER TABLE person RENAME COLUMN namesofotherguardians TO namesofguardians;
ALTER TABLE person_history RENAME COLUMN namesofotherguardians TO namesofguardians;

INSERT INTO schema_version (version_number, comment) VALUES (296, 'Change namesOfOtherGuardians to namesOfGuardians #3413');

-- 2020-12-7 Add a means of transports field to events #3618
ALTER TABLE events ADD COLUMN meansOfTransport varchar(255);
ALTER TABLE events_history ADD COLUMN meansOfTransport varchar(255);
ALTER TABLE events ADD COLUMN meansOfTransportDetails text;
ALTER TABLE events_history ADD COLUMN meansOfTransportDetails text;

INSERT INTO schema_version (version_number, comment) VALUES (297, 'Add a means of transports field to events #3618');

-- 2021-01-05 Add reporting district to cases & contacts #3410
ALTER TABLE cases ADD COLUMN reportingdistrict_id bigint;
ALTER TABLE cases
    ADD CONSTRAINT fk_cases_reportingdistrict_id FOREIGN KEY (reportingdistrict_id) REFERENCES district(id);

ALTER TABLE cases_history ADD COLUMN reportingdistrict_id bigint;
ALTER TABLE cases_history
    ADD CONSTRAINT fk_cases_history_reportingdistrict_id FOREIGN KEY (reportingdistrict_id) REFERENCES district(id);

ALTER TABLE contact ADD COLUMN reportingdistrict_id bigint;
ALTER TABLE contact
    ADD CONSTRAINT fk_contact_reportingdistrict_id FOREIGN KEY (reportingdistrict_id) REFERENCES district(id);

ALTER TABLE contact_history ADD COLUMN reportingdistrict_id bigint;
ALTER TABLE contact_history
    ADD CONSTRAINT fk_contact_history_reportingdistrict_id FOREIGN KEY (reportingdistrict_id) REFERENCES district(id);

INSERT INTO schema_version (version_number, comment) VALUES (298, 'Add reporting district to cases & contacts #3410');

-- 2021-01-07 Add index for resulting cases of contacts #3926
CREATE INDEX IF NOT EXISTS idx_contact_resultingcase_id ON contact USING hash (resultingcase_id);

INSERT INTO schema_version (version_number, comment) VALUES (299, 'Add index for resulting cases of contacts #3926');

-- 2021-01-05 Type of place details in events entities #2947
ALTER TABLE events ADD COLUMN connectionNumber varchar(512);
ALTER TABLE events_history ADD COLUMN connectionNumber varchar(512);
ALTER TABLE events ADD COLUMN travelDate timestamp without time zone;
ALTER TABLE events_history ADD COLUMN travelDate timestamp without time zone;

INSERT INTO schema_version (version_number, comment) VALUES (300, 'Type of place details in events entities #2947');

-- 2020-01-04 Change action's columns description and reply type from varchar to text #3848
ALTER TABLE action ALTER COLUMN description TYPE text;
ALTER TABLE action_history ALTER COLUMN description TYPE text;
ALTER TABLE action ALTER COLUMN reply TYPE text;
ALTER TABLE action_history ALTER COLUMN reply TYPE text;

INSERT INTO schema_version (version_number, comment) VALUES (301, 'Change action''s columns description and reply type from varchar to text #3848');

-- 2020-12-03 Remove hospital from event's type of place #3617
-- 2021-01-28 [Hotfix] Fixed migration code setting facility type for all locations in the system #4120
UPDATE location SET facilitytype = 'HOSPITAL' WHERE facilitytype IS NULL AND (SELECT typeofplace FROM events WHERE eventlocation_id = location.id) = 'HOSPITAL';
UPDATE events SET typeofplace = 'FACILITY' WHERE (SELECT facilitytype FROM location WHERE id = events.eventlocation_id) IS NOT NULL;

INSERT INTO schema_version (version_number, comment) VALUES (302, 'Remove hospital from event''s type of place #3617, #4120');

-- 2020-01-11 SurvNet Adaptation - Dedicated fields for technical and non-technical external IDs #3524
ALTER TABLE cases ADD COLUMN externaltoken varchar(512);
ALTER TABLE cases_history ADD COLUMN externaltoken varchar(512);

ALTER TABLE contact ADD COLUMN externaltoken varchar(512);
ALTER TABLE contact_history ADD COLUMN externaltoken varchar(512);
-- increasing person and person_history externalid size without loosing data.
ALTER TABLE person ALTER COLUMN externalid type character varying (512);
ALTER TABLE person_history ALTER COLUMN externalid type character varying (512);

ALTER TABLE person ADD COLUMN externaltoken varchar(512);
ALTER TABLE person_history ADD COLUMN externaltoken varchar(512);

ALTER TABLE events ADD COLUMN externaltoken varchar(512);
ALTER TABLE events_history ADD COLUMN externaltoken varchar(512);

INSERT INTO schema_version (version_number, comment) VALUES (303, 'SurvNet Adaptation - Dedicated fields for technical and non-technical external IDs #3524');

-- 2021-01-07 Add system events #3927
CREATE TABLE systemevent (
    id bigint not null,
    uuid varchar(36) not null unique,
    changedate timestamp not null,
    creationdate timestamp not null,
    type varchar(255) not null,
    startdate timestamp not null,
    enddate timestamp,
    status varchar(255) not null,
    additionalInfo text,
    primary key(id)
);

ALTER TABLE systemevent OWNER TO sormas_user;

INSERT INTO schema_version (version_number, comment) VALUES (304, 'Add system events #3927');

-- 2020-12-17 Change action's replyingUser to lastModifiedBy #3719
ALTER TABLE action RENAME COLUMN replyinguser_id TO lastmodifiedby_id;
ALTER TABLE action_history RENAME COLUMN replyinguser_id TO lastmodifiedby_id;

INSERT INTO schema_version (version_number, comment) VALUES (305, 'Change action''s replyingUser to lastModifiedBy #3719');

-- 2021-01-14 - Add new fields to outbreak events needed for SurvNet #4013
ALTER TABLE action ADD COLUMN actionmeasure varchar(255);
ALTER TABLE action_history ADD COLUMN actionmeasure varchar(255);
ALTER TABLE events ADD COLUMN transregionaloutbreak varchar(255);
ALTER TABLE events_history ADD COLUMN transregionaloutbreak varchar(255);
ALTER TABLE events ADD COLUMN diseasetransmissionmode varchar(255);
ALTER TABLE events_history ADD COLUMN diseasetransmissionmode varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (306, 'Add new fields to outbreak events needed for SurvNet #4013');

-- 2020-01-12 Store sormas to sormas share options #3763
ALTER TABLE sormastosormasshareinfo
    ADD COLUMN withassociatedcontacts boolean DEFAULT false,
    ADD COLUMN withsamples boolean DEFAULT false,
    ADD COLUMN pseudonymizedpersonaldata boolean DEFAULT false,
    ADD COLUMN pseudonymizedsensitivedata boolean DEFAULT false;

INSERT INTO schema_version (version_number, comment) VALUES (307, 'Store sormas to sormas share options #3763');

-- 2021-01-15 - Add superordinate event to events #4020
ALTER TABLE events ADD COLUMN superordinateevent_id bigint;
ALTER TABLE events_history ADD COLUMN superordinateevent_id bigint;

ALTER TABLE events ADD CONSTRAINT fk_events_superordinateevent_id FOREIGN KEY (superordinateevent_id) REFERENCES events(id);
CREATE INDEX IF NOT EXISTS idx_events_superordinateevent_id ON events USING hash (superordinateevent_id);

INSERT INTO schema_version (version_number, comment) VALUES (308, 'Add superordinate event to events #4020');

-- 2020-12-03 Remove hospital from exposure type of places #3680
-- 2021-01-28 [Hotfix] Fixed migration code setting facility type for all locations in the system #4120
UPDATE location SET facilitytype = 'HOSPITAL' WHERE facilitytype IS NULL AND (SELECT typeofplace FROM exposures WHERE location_id = location.id) = 'HOSPITAL';
UPDATE exposures SET typeofplace = 'FACILITY' WHERE (SELECT facilitytype FROM location WHERE id = exposures.location_id) IS NOT NULL;

INSERT INTO schema_version (version_number, comment) VALUES (309, 'Remove hospital from exposure type of places #3680, #4120');

-- 2020-12-21 Fix labmessage table #3486
ALTER TABLE labmessage OWNER TO sormas_user;
CREATE TRIGGER versioning_trigger BEFORE INSERT OR UPDATE OR DELETE ON labmessage
    FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'labmessage_history', true);
ALTER TABLE labmessage_history OWNER TO sormas_user;
ALTER TABLE labmessage ADD COLUMN messagedatetime timestamp;
ALTER TABLE labmessage_history ADD COLUMN messagedatetime timestamp;

INSERT INTO schema_version (version_number, comment) VALUES (310, 'Fix labmessage table #3486');

-- 2021-01-07 Add evolution date and comment to events #3753
ALTER TABLE events ADD COLUMN evolutionDate timestamp;
ALTER TABLE events_history ADD COLUMN evolutionDate timestamp;
ALTER TABLE events ADD COLUMN evolutionComment text;
ALTER TABLE events_history ADD COLUMN evolutionComment text;

INSERT INTO schema_version (version_number, comment) VALUES (311, 'Add evolution date and comment to events #3753');

-- 2020-01-13 Add indexes to optimize event directory performance #3276
CREATE INDEX IF NOT EXISTS idx_eventparticipant_person_id ON eventparticipant USING hash (person_id);
CREATE INDEX IF NOT EXISTS idx_eventparticipant_event_id ON eventparticipant USING hash (event_id);
CREATE INDEX IF NOT EXISTS idx_contact_person_id ON contact USING hash (person_id);

INSERT INTO schema_version (version_number, comment) VALUES (312, 'Add indexes to optimize event directory performance #3276');

-- 2020-01-27
ALTER TABLE exportconfiguration
    ADD COLUMN sharedToPublic boolean default false;

ALTER TABLE exportconfiguration_history
    ADD COLUMN sharedToPublic boolean default false;

INSERT INTO schema_version (version_number, comment) VALUES (313, 'Allow specific users to create public custom exports #1754');

-- 2021-01-11 Add testresulttext to labmessage #3820
ALTER TABLE labmessage ADD COLUMN testresulttext TEXT;
ALTER TABLE labmessage_history ADD COLUMN testresulttext TEXT;

INSERT INTO schema_version (version_number, comment) VALUES (314, 'Add testresulttext to labmessage #3820');

-- 2021-02-03 Activate vaccination status for COVID-19 cases, contacts and event participant #4137
ALTER TABLE cases
    RENAME COLUMN vaccinationdate TO lastvaccinationdate;
ALTER TABLE cases
    ADD COLUMN firstvaccinationdate timestamp,
    ADD COLUMN vaccinename varchar(255),
    ADD COLUMN othervaccinename text,
    ADD COLUMN vaccinemanufacturer varchar(255),
    ADD COLUMN othervaccinemanufacturer text,
    ADD COLUMN vaccineinn text,
    ADD COLUMN vaccinebatchnumber text,
    ADD COLUMN vaccineuniicode text,
    ADD COLUMN vaccineatccode text;

ALTER TABLE cases_history
    RENAME COLUMN vaccinationdate TO lastvaccinationdate;
ALTER TABLE cases_history
    ADD COLUMN firstvaccinationdate timestamp,
    ADD COLUMN vaccinename varchar(255),
    ADD COLUMN othervaccinename text,
    ADD COLUMN vaccinemanufacturer varchar(255),
    ADD COLUMN othervaccinemanufacturer text,
    ADD COLUMN vaccineinn text,
    ADD COLUMN vaccinebatchnumber text,
    ADD COLUMN vaccineuniicode text,
    ADD COLUMN vaccineatccode text;

INSERT INTO schema_version (version_number, comment) VALUES (315, 'Activate vaccination status for COVID-19 cases, contacts and event participant #4137');

-- 2021-01-19 Add DiseaseVariant entity #4042
CREATE TABLE diseasevariant(
	id bigint not null,
	uuid varchar(36) not null unique,
	changedate timestamp not null,
	creationdate timestamp not null,
	disease varchar(255) not null,
	name varchar(512) not null,
	sys_period tstzrange not null,
	primary key(id));

ALTER TABLE diseasevariant OWNER TO sormas_user;

CREATE TABLE diseasevariant_history (LIKE diseasevariant);
CREATE TRIGGER versioning_trigger
BEFORE INSERT OR UPDATE OR DELETE ON diseasevariant
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'diseasevariant_history', true);
ALTER TABLE diseasevariant_history OWNER TO sormas_user;

ALTER TABLE cases ADD COLUMN diseasevariant_id bigint;
ALTER TABLE cases_history ADD COLUMN diseasevariant_id bigint;
ALTER TABLE cases ADD CONSTRAINT fk_cases_diseasevariant_id FOREIGN KEY (diseasevariant_id) REFERENCES diseasevariant(id);

INSERT INTO schema_version (version_number, comment) VALUES (316, 'Add DiseaseVariant entity #4042');

 -- 2020-02-03
ALTER TABLE pathogentest ADD COLUMN typingId text;
ALTER TABLE pathogentest_history ADD COLUMN typingId text;

INSERT INTO schema_version (version_number, comment) VALUES (317, 'Add typing ID to pathogen tests #3957');

-- 2021-01-07 Change event's surveillanceOfficer to responsibleUser allow more roles to be it #3827
ALTER TABLE events RENAME surveillanceofficer_id to responsibleuser_id;
ALTER TABLE events_history RENAME surveillanceofficer_id to responsibleuser_id;

INSERT INTO schema_version (version_number, comment) VALUES (318, 'Change event''s surveillanceOfficer to responsibleUser allow more roles to be it #3827');

--2020-02-02
ALTER TABLE exposures RENAME patientexpositionrole TO exposureRole;
ALTER TABLE exposures_history RENAME patientexpositionrole TO exposureRole;

UPDATE exposures SET exposureRole = NULL WHERE exposureRole = 'NOT_COLLECTED';
UPDATE exposures SET exposureRole = 'STAFF' WHERE exposureRole = 'WORKING_AT';
UPDATE exposures SET exposureRole = 'GUEST' WHERE exposureRole = 'ACCOMMODATED_IN';
UPDATE exposures SET exposureRole = 'PATIENT' WHERE exposureRole = 'CARED_FOR';

INSERT INTO schema_version (version_number, comment) VALUES (319, '[SurvNet Interface] Change title of role field in exposures and add new field #4036');
-- 2021-02-04 Add vaccination for contacts and event participant #4137

CREATE TABLE vaccinationinfo (
    id bigint NOT NULL,
    changedate timestamp without time zone NOT NULL,
    creationdate timestamp without time zone NOT NULL,
    uuid character varying(36) NOT NULL,
    vaccination varchar(255),
    vaccinationdoses varchar(512),
    vaccinationinfosource varchar(255),
    firstvaccinationdate timestamp,
    lastvaccinationdate timestamp,
    vaccinename varchar(255),
    othervaccinename text,
    vaccinemanufacturer varchar(255),
    othervaccinemanufacturer text,
    vaccineinn text,
    vaccinebatchnumber text,
    vaccineuniicode text,
    vaccineatccode text,
    sys_period tstzrange not null,
    primary key (id)
);

ALTER TABLE vaccinationinfo OWNER TO sormas_user;

CREATE TABLE vaccinationinfo_history (LIKE vaccinationinfo);
CREATE TRIGGER versioning_trigger
    BEFORE INSERT OR UPDATE OR DELETE ON vaccinationinfo
    FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'vaccinationinfo_history', true);
ALTER TABLE vaccinationinfo_history OWNER TO sormas_user;

ALTER TABLE contact
    ADD COLUMN vaccinationinfo_id bigint;

ALTER TABLE contact ADD CONSTRAINT fk_contact_vaccinationinfo_id FOREIGN KEY (vaccinationinfo_id) REFERENCES vaccinationinfo(id);

ALTER TABLE eventparticipant
    ADD COLUMN vaccinationinfo_id bigint;

ALTER TABLE eventparticipant ADD CONSTRAINT fk_eventparticipant_vaccinationinfo_id FOREIGN KEY (vaccinationinfo_id) REFERENCES vaccinationinfo(id);

DO $$
    DECLARE rec RECORD;
        DECLARE new_vaccination_info_id INTEGER;
    BEGIN
        FOR rec IN SELECT id FROM public.contact WHERE contact.vaccinationinfo_id IS NULL
            LOOP
                INSERT INTO vaccinationinfo(id, uuid, creationdate, changedate) VALUES (nextval('entity_seq'), upper(substring(CAST(CAST(md5(CAST(random() AS text) || CAST(clock_timestamp() AS text)) AS uuid) AS text), 3, 29)), now(), now()) RETURNING id INTO new_vaccination_info_id;
                UPDATE contact SET vaccinationinfo_id = new_vaccination_info_id WHERE id = rec.id;
            END LOOP;

        FOR rec IN SELECT id FROM public.eventparticipant WHERE eventparticipant.vaccinationinfo_id IS NULL
            LOOP
                INSERT INTO vaccinationinfo(id, uuid, creationdate, changedate) VALUES (nextval('entity_seq'), upper(substring(CAST(CAST(md5(CAST(random() AS text) || CAST(clock_timestamp() AS text)) AS uuid) AS text), 3, 29)), now(), now()) RETURNING id INTO new_vaccination_info_id;
                UPDATE eventparticipant SET vaccinationinfo_id = new_vaccination_info_id WHERE id = rec.id;
            END LOOP;
    END;
$$ LANGUAGE plpgsql;

INSERT INTO schema_version (version_number, comment) VALUES (320, 'Add vaccination for contacts and event participant #4137');


-- 2021-02-04 - [SurvNet Interface] Add fields next to type of place #4038
ALTER TABLE exposures ADD COLUMN workenvironment varchar(255);
ALTER TABLE exposures_history ADD COLUMN workenvironment varchar(255);
ALTER TABLE events ADD COLUMN workenvironment varchar(255);
ALTER TABLE events_history ADD COLUMN workenvironment varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (321, '[SurvNet Interface] Add fields next to type of place #4038');

-- 2020-02-08 Add UNIQUE constraint to documents uuid #3661
ALTER TABLE ONLY documents ADD CONSTRAINT documents_uuid_key UNIQUE (uuid);

INSERT INTO schema_version (version_number, comment) VALUES (322, 'Add UNIQUE contraint to documents uuid field #3661');

-- 2020-02-08 SurvNet Adaptations - Create new field “Blood donation in the last 6 months” for cases #3414
ALTER TABLE cases ADD COLUMN bloodorganortissuedonated varchar(255);
ALTER TABLE cases_history ADD COLUMN bloodorganortissuedonated varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (323, '2020-02-08 SurvNet Adaptations - Create new field “Blood donation in the last 6 months” for cases #3414');

-- 2021-02-05 Case identification source #3420
ALTER TABLE cases ADD COLUMN caseidentificationsource character varying(255);
ALTER TABLE cases_history ADD COLUMN caseidentificationsource character varying(255);

INSERT INTO schema_version (version_number, comment) VALUES (324, 'Case identification source #3420');

-- 2021-02-10 SurvNet Adaptations - Create new field “Suspicious case” to cases

ALTER TABLE cases ADD COLUMN notACaseReasonNegativeTest boolean default false;
ALTER TABLE cases_history ADD COLUMN notACaseReasonNegativeTest boolean default false;

ALTER TABLE cases ADD COLUMN notACaseReasonPhysicianInformation boolean default false;
ALTER TABLE cases_history ADD COLUMN notACaseReasonPhysicianInformation boolean default false;

ALTER TABLE cases ADD COLUMN notACaseReasonDifferentPathogen boolean default false;
ALTER TABLE cases_history ADD COLUMN notACaseReasonDifferentPathogen boolean default false;

ALTER TABLE cases ADD COLUMN notACaseReasonOther boolean default false;
ALTER TABLE cases_history ADD COLUMN notACaseReasonOther boolean default false;

ALTER TABLE cases ADD COLUMN notACaseReasonDetails text;
ALTER TABLE cases_history ADD COLUMN notACaseReasonDetails text;

INSERT INTO schema_version (version_number, comment) VALUES (325, 'SurvNet Adaptations - Create new field “Suspicious case” to cases #3419');

-- 2020-02-09 #3831 SurvNet Adaptations - Create new field “Reinfection” for cases
ALTER TABLE cases
    ADD COLUMN reinfection varchar(255),
    ADD COLUMN previousinfectiondate timestamp;

ALTER TABLE cases_history
    ADD COLUMN reinfection varchar(255),
    ADD COLUMN previousinfectiondate timestamp;

INSERT INTO schema_version (version_number, comment) VALUES (326, 'SurvNet Adaptations - Create new field “Reinfection” for cases #3831');

-- 2021-02-10 - Make user roles deactivateable #3716
ALTER TABLE userrolesconfig ADD COLUMN enabled boolean NOT NULL default true;
ALTER TABLE userrolesconfig_history ADD COLUMN enabled boolean NOT NULL default true;

INSERT INTO schema_version (version_number, comment) VALUES (327, 'Make user roles deactivateable #3716');

-- 2020-02-12 Remove locations assigned to more than one exposure from deleted cases #4338
ALTER TABLE exposures ALTER COLUMN location_id DROP NOT NULL;
ALTER TABLE exposures_history ALTER COLUMN location_id DROP NOT NULL;
UPDATE exposures SET location_id = null FROM cases WHERE cases.epidata_id = exposures.epidata_id AND cases.deleted IS true AND (SELECT COUNT(location_id) FROM exposures ex WHERE ex.location_id = exposures.location_id) > 1;

INSERT INTO schema_version (version_number, comment) VALUES (328, 'Remove locations assigned to more than one exposure from deleted cases #4338');

-- 2020-02-15 Add missing indexes #3481
CREATE INDEX IF NOT EXISTS idx_samples_associatedcase_id ON samples USING btree (associatedcase_id);
CREATE INDEX IF NOT EXISTS idx_eventparticipant_reporting_user_id ON eventparticipant USING btree (reportinguser_id);
CREATE INDEX IF NOT EXISTS idx_cases_reporting_user_id ON cases USING hash (reportinguser_id);
CREATE INDEX IF NOT EXISTS idx_cases_person_id ON cases USING btree (person_id);
CREATE INDEX IF NOT EXISTS idx_contact_reporting_user_id ON contact USING btree (reportinguser_id);
CREATE INDEX IF NOT EXISTS idx_diseaseconfiguration_changedate on diseaseconfiguration (changedate DESC);
CREATE INDEX IF NOT EXISTS idx_person_uuid ON person USING hash(uuid);
CREATE INDEX IF NOT EXISTS idx_contact_uuid ON contact USING hash(uuid);
CREATE INDEX IF NOT EXISTS idx_users_uuid ON users USING hash(uuid);
CREATE INDEX IF NOT EXISTS idx_users_username ON users USING hash(username);

INSERT INTO schema_version (version_number, comment) VALUES (329, 'evaluate performance cases #3481');

-- 2020-02-12 [SurvNet Interface] Add Reports to case information #4282

CREATE TABLE surveillancereports (
    id bigint NOT NULL,
    changedate timestamp without time zone NOT NULL,
    creationdate timestamp without time zone NOT NULL,
    uuid character varying(36) NOT NULL,
    reportingtype varchar(255),
    creatinguser_id bigint,
    reportdate timestamp NOT NULL,
    dateofdiagnosis timestamp,
    facilityregion_id bigint,
    facilitydistrict_id bigint,
    facilitytype varchar(255),
    facility_id bigint,
    facilitydetails text,
    notificationdetails text,
    caze_id bigint,
    sys_period tstzrange not null,
    primary key (id)
);

ALTER TABLE surveillancereports OWNER TO sormas_user;

ALTER TABLE surveillancereports ADD CONSTRAINT fk_surveillancereports_creatinguser_id FOREIGN KEY (creatinguser_id) REFERENCES users(id);
ALTER TABLE surveillancereports ADD CONSTRAINT fk_surveillancereports_facilityregion_id FOREIGN KEY (facilityregion_id) REFERENCES region(id);
ALTER TABLE surveillancereports ADD CONSTRAINT fk_surveillancereports_facilitydistrict_id FOREIGN KEY (facilitydistrict_id) REFERENCES district(id);
ALTER TABLE surveillancereports ADD CONSTRAINT fk_surveillancereports_facility_id FOREIGN KEY (facility_id) REFERENCES facility(id);
ALTER TABLE surveillancereports ADD CONSTRAINT fk_surveillancereports_caze_id FOREIGN KEY (caze_id) REFERENCES cases(id);

CREATE TABLE surveillancereports_history (LIKE surveillancereports);
CREATE TRIGGER versioning_trigger
    BEFORE INSERT OR UPDATE OR DELETE ON surveillancereports
    FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'surveillancereports_history', true);
ALTER TABLE surveillancereports_history OWNER TO sormas_user;

DO $$
    DECLARE rec RECORD;
    BEGIN
        FOR rec IN SELECT id as _caze_id, reportingtype as _reportingtype, reportdate as _reportdate, reportinguser_id as _reportinguser_id
        FROM public.cases WHERE cases.reportingtype IS NOT NULL and cases.reportingtype <> 'LABORATORY'
            LOOP
                INSERT INTO surveillancereports(id, uuid, creationdate, changedate, reportingtype, reportdate, creatinguser_id, caze_id)
                VALUES (nextval('entity_seq'),
                        overlay(overlay(overlay(
                            substring(upper(REPLACE(CAST(CAST(md5(CAST(random() AS text) || CAST(clock_timestamp() AS text)) AS uuid) AS text), '-', '')), 0, 30)
                            placing '-' from 7) placing '-' from 14) placing '-' from 21),
                        now(), now(),
                        rec._reportingtype, rec._reportdate, rec._reportinguser_id, rec._caze_id);
            END LOOP;
    END;
$$ LANGUAGE plpgsql;

ALTER TABLE cases DROP COLUMN reportingtype;

INSERT INTO schema_version (version_number, comment) VALUES (330, '[SurvNet Interface] Add Reports to case information #4282');

-- 2021-02-05 Add reason hospitalization #4187
ALTER TABLE hospitalization ADD COLUMN hospitalizationreason varchar(255);
ALTER TABLE hospitalization_history ADD COLUMN hospitalizationreason varchar(255);

ALTER TABLE hospitalization ADD COLUMN otherhospitalizationreason text;
ALTER TABLE hospitalization_history ADD COLUMN otherhospitalizationreason text;

ALTER TABLE previoushospitalization ADD COLUMN hospitalizationreason varchar(255);
ALTER TABLE previoushospitalization_history ADD COLUMN hospitalizationreason varchar(255);

ALTER TABLE previoushospitalization ADD COLUMN otherhospitalizationreason text;
ALTER TABLE previoushospitalization_history ADD COLUMN otherhospitalizationreason text;

INSERT INTO schema_version (version_number, comment) VALUES (331, '#4187 add reason for hospitalization');

-- 2021-02-15 Add reportDate to pathogen test #4363
ALTER TABLE pathogentest ADD COLUMN reportdate timestamp;
ALTER TABLE pathogentest_history ADD COLUMN reportdate timestamp;

INSERT INTO schema_version (version_number, comment) VALUES (332, 'Add reportDate to pathogen test #4363');

-- 2021-02-15 Case identification source - screening #3420
ALTER TABLE cases ADD COLUMN screeningtype character varying(255);
ALTER TABLE cases_history ADD COLUMN screeningtype character varying(255);
UPDATE cases SET screeningtype = 'ON_HOSPITAL_ADMISSION', caseidentificationsource = 'SCREENING' where caseidentificationsource = 'ON_HOSPITAL_ADMISSION';
UPDATE cases SET screeningtype = 'ON_CARE_HOME_ADMISSION', caseidentificationsource = 'SCREENING' where caseidentificationsource = 'ON_CARE_HOME_ADMISSION';
UPDATE cases SET screeningtype = 'ON_ASYLUM_ADMISSION', caseidentificationsource = 'SCREENING' where caseidentificationsource = 'ON_ASYLUM_ADMISSION';
UPDATE cases SET screeningtype = 'ON_ENTRY_FROM_RISK_AREA', caseidentificationsource = 'SCREENING' where caseidentificationsource = 'ON_ENTRY_FROM_RISK_AREA';
UPDATE cases SET screeningtype = 'HEALTH_SECTOR_EMPLOYEE', caseidentificationsource = 'SCREENING' where caseidentificationsource = 'HEALTH_SECTOR_EMPLOYEE';
UPDATE cases SET screeningtype = 'EDUCATIONAL_INSTITUTIONS', caseidentificationsource = 'SCREENING' where caseidentificationsource = 'EDUCATIONAL_INSTITUTIONS';

INSERT INTO schema_version (version_number, comment) VALUES (333, 'Case identification source - screening type #3420');

-- 2021-02-16 - Make user roles deactivateable #3716
-- initial deploy was in schema version 327 but without "default true" statement. This has been installed on most servers
-- but on some servers which had data in userrolesconfig table the change crashed as we would need to add a default value
-- in order to make script available in both situations on already installed and on crashed servers the change consists in
--  a) add "default true" statement to schema version 327 to resolve the servers which are crashing
--  b) add schema version 334 in order to add "default true" to servers which ran already version 327 and need the default true for future use
ALTER TABLE userrolesconfig DROP COLUMN IF EXISTS enabled;
ALTER TABLE userrolesconfig_history DROP COLUMN IF EXISTS enabled;

ALTER TABLE userrolesconfig ADD COLUMN enabled boolean NOT NULL default true;
ALTER TABLE userrolesconfig_history ADD COLUMN enabled boolean NOT NULL default true;

INSERT INTO schema_version (version_number, comment) VALUES (334, 'Make user roles deactivateable #3716');

-- 2020-02-09 Add indexes #4307
CREATE INDEX IF NOT EXISTS idx_cases_epid_number ON cases USING gist (epidnumber gist_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_cases_person_id ON cases (person_id);
CREATE INDEX IF NOT EXISTS idx_cases_region_id ON cases (region_id);
CREATE INDEX IF NOT EXISTS idx_cases_district_id ON cases (district_id);
CREATE INDEX IF NOT EXISTS idx_cases_disease ON cases (disease);

CREATE INDEX IF NOT EXISTS idx_contact_region_id ON contact (region_id);
CREATE INDEX IF NOT EXISTS idx_contact_district_id ON contact (district_id);
CREATE INDEX IF NOT EXISTS idx_contact_case_id ON contact (caze_id);

CREATE INDEX IF NOT EXISTS idx_eventparticipant_event_id ON eventparticipant (event_id);
CREATE INDEX IF NOT EXISTS idx_eventparticipant_person_id ON eventparticipant (person_id);
CREATE INDEX IF NOT EXISTS idx_eventparticipant_resultingcase_id ON eventparticipant (resultingcase_id);

CREATE INDEX IF NOT EXISTS idx_samples_associatedcontact_id ON samples (associatedcontact_id);
CREATE INDEX IF NOT EXISTS idx_samples_associatedcase_id ON samples (associatedcase_id);
CREATE INDEX IF NOT EXISTS idx_samples_associatedeventparticipant_id ON samples (associatedeventparticipant_id);
CREATE INDEX IF NOT EXISTS idx_samples_lab_id ON samples (lab_id);

CREATE INDEX IF NOT EXISTS idx_task_contact_id ON task (contact_id);
CREATE INDEX IF NOT EXISTS idx_task_case_id ON task (caze_id);
CREATE INDEX IF NOT EXISTS idx_task_event_id ON task (event_id);

CREATE INDEX IF NOT EXISTS idx_visit_person_id ON visit (person_id);

CREATE INDEX IF NOT EXISTS idx_pathogentest_sample_id ON pathogentest (sample_id);

CREATE INDEX IF NOT EXISTS idx_additionaltest_sample_id ON additionaltest (sample_id);

CREATE INDEX IF NOT EXISTS idx_outbreak_district_id ON outbreak (district_id);

CREATE UNIQUE INDEX IF NOT EXISTS idx_events_location_id ON events (eventlocation_id);

CREATE INDEX IF NOT EXISTS idx_location_region_id ON location (region_id);
CREATE INDEX IF NOT EXISTS idx_location_district_id ON location (district_id);

CREATE INDEX IF NOT EXISTS idx_facility_region_id ON facility (region_id);
CREATE INDEX IF NOT EXISTS idx_facility_district_id ON facility (district_id);

CREATE INDEX IF NOT EXISTS idx_exposures_epidata_id ON exposures (epidata_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_exposures_location_id ON exposures (location_id);

CREATE INDEX IF NOT EXISTS idx_previoushospitalization_hospitalization_id ON previoushospitalization (hospitalization_id);

INSERT INTO schema_version (version_number, comment) VALUES (335, '2020-02-09 Add indexes #4307');

-- 2021-02-16 - [SurvNet Interface] Care/accommodation/work in facility #4163
ALTER TABLE epidata ADD COLUMN activityascasedetailsknown varchar(255);
ALTER TABLE epidata_history ADD COLUMN activityascasedetailsknown varchar(255);

CREATE TABLE activityascase(
      id bigint not null,
      uuid varchar(36) not null unique,
      changedate timestamp not null,
      creationdate timestamp not null,
      epidata_id bigint not null,
      reportinguser_id bigint,
      startdate timestamp,
      enddate timestamp,
      description text,
      activityAsCaseType varchar(255) not null,
      activityAsCaseTypeDetails text,
      location_id bigint not null,
      role varchar(255),
      typeofplace varchar(255),
      typeofplacedetails text,
      meansoftransport varchar(255),
      meansoftransportdetails text,
      connectionnumber varchar(512),
      seatnumber varchar(512),
      workEnvironment varchar(255),

      gatheringtype varchar(255),
      gatheringdetails text,
      habitationtype varchar(255),
      habitationdetails text,
      typeofanimal varchar(255),
      typeofanimaldetails text,

      sys_period tstzrange not null,
      primary key(id)
);

ALTER TABLE activityascase OWNER TO sormas_user;
ALTER TABLE activityascase ADD CONSTRAINT fk_activityascase_epidata_id FOREIGN KEY (epidata_id) REFERENCES epidata(id);
ALTER TABLE activityascase ADD CONSTRAINT fk_activityascase_reportinguser_id FOREIGN KEY (reportinguser_id) REFERENCES users(id);
ALTER TABLE activityascase ADD CONSTRAINT fk_activityascase_location_id FOREIGN KEY (location_id) REFERENCES location(id);

CREATE TABLE activityascase_history (LIKE activityascase);
CREATE TRIGGER versioning_trigger BEFORE INSERT OR UPDATE OR DELETE ON activityascase
    FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'activityascase_history', true);
ALTER TABLE activityascase_history OWNER TO sormas_user;

INSERT INTO schema_version (version_number, comment) VALUES (336, '[SurvNet Interface] Care/accommodation/work in facility #4163');

-- 2020-02-18 Add Country to location details #2994
ALTER TABLE location ADD COLUMN country_id bigint;
ALTER TABLE location_history ADD COLUMN country_id bigint;
ALTER TABLE location ADD CONSTRAINT fk_location_country_id FOREIGN KEY (country_id) REFERENCES country(id);

INSERT INTO schema_version (version_number, comment) VALUES (337, 'Add Country to location details #2994');

-- 2020-02-12 [SORMAS 2 SORMAS] Send and receive Events #4348
ALTER TABLE events ADD COLUMN sormasToSormasOriginInfo_id bigint;
ALTER TABLE events ADD CONSTRAINT fk_events_sormasToSormasOriginInfo_id FOREIGN KEY (sormasToSormasOriginInfo_id) REFERENCES sormastosormasorigininfo (id) ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE eventparticipant ADD COLUMN sormasToSormasOriginInfo_id bigint;
ALTER TABLE eventparticipant ADD CONSTRAINT fk_eventparticipant_sormasToSormasOriginInfo_id FOREIGN KEY (sormasToSormasOriginInfo_id) REFERENCES sormastosormasorigininfo (id) ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE sormastosormasshareinfo ADD COLUMN event_id bigint;
ALTER TABLE sormastosormasshareinfo ADD CONSTRAINT fk_sormastosormasshareinfo_event_id FOREIGN KEY (event_id) REFERENCES events (id) ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE sormastosormasshareinfo
    ADD COLUMN eventparticipant_id bigint,
    ADD COLUMN witheventparticipants boolean DEFAULT false;
ALTER TABLE sormastosormasshareinfo ADD CONSTRAINT fk_sormastosormasshareinfo_eventparticipant_id FOREIGN KEY (eventparticipant_id) REFERENCES eventparticipant (id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO schema_version (version_number, comment) VALUES (338, '[SORMAS 2 SORMAS] Send and receive Events #4348');

-- 2021-02-28 Add optional translation to CampaignDiagramDefinition #4090
ALTER TABLE campaigndiagramdefinition ADD COLUMN campaigndiagramtranslations json;
ALTER TABLE campaigndiagramdefinition_history ADD COLUMN campaigndiagramtranslations json;

INSERT INTO schema_version (version_number, comment) VALUES (339, 'Add optional translation to CampaignDiagramDefinition #4090');

-- 2021-03-03 Add facilities' address #4027
ALTER TABLE facility ADD COLUMN street varchar(4096);
ALTER TABLE facility ADD COLUMN housenumber varchar(512);
ALTER TABLE facility ADD COLUMN additionalinformation varchar(255);
ALTER TABLE facility ADD COLUMN postalcode varchar(512);
ALTER TABLE facility ADD COLUMN areatype varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (340, 'Add facilities address #4027');

-- 2021-02-23 Add event management status to Event #4255

ALTER TABLE events ADD COLUMN eventmanagementstatus varchar(255);
ALTER TABLE events_history ADD COLUMN eventmanagementstatus varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (341, 'Add event management status to Event #4255');

-- 2021-03-04 Extend exposure #4549
ALTER TABLE exposures ADD COLUMN largeattendancenumber varchar(255);
ALTER TABLE exposures_history ADD COLUMN largeattendancenumber varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (342, 'Extend exposure #4549');

-- 2021-03-04 Add person email, person phone, testResultVerified to LabMessage #4106
ALTER TABLE labmessage ADD COLUMN personphone VARCHAR(255);
ALTER TABLE labmessage ADD COLUMN personemail VARCHAR(255);
ALTER TABLE labmessage_history ADD COLUMN personphone VARCHAR(255);
ALTER TABLE labmessage_history ADD COLUMN personemail VARCHAR(255);
ALTER TABLE labmessage ADD COLUMN testresultverified boolean;
ALTER TABLE labmessage_history ADD COLUMN testresultverified boolean;
UPDATE labmessage SET testresultverified = true;

INSERT INTO schema_version (version_number, comment) VALUES (343, 'Add person email, person phone, testResultVerified to LabMessage #4106');

-- 2021-03-03 Add a "sampling reason" field in the sample #4555
ALTER TABLE samples
    ADD COLUMN samplingreason varchar(255),
    ADD COLUMN samplingreasondetails text;
ALTER TABLE samples_history
    ADD COLUMN samplingreason varchar(255),
    ADD COLUMN samplingreasondetails text;

DO $$
    DECLARE rec RECORD;
        DECLARE latest_sample RECORD;
        DECLARE _samplingreason text;
    BEGIN
        FOR rec IN SELECT id as _caseid, covidtestreason as _covidtestreason, covidtestreasondetails as _covidtestreasondetails, reportdate as _reportdate, reportinguser_id as _reportinguser_id
                   FROM cases WHERE cases.covidtestreason IS NOT NULL
            LOOP
                _samplingreason = CASE WHEN rec._covidtestreason='REQUIREMENT_OF_EMPLOYER' THEN 'PROFESSIONAL_REASON'
                                       WHEN rec._covidtestreason='DURING_QUARANTINE' THEN 'QUARANTINE_REGULATIONS'
                                       WHEN rec._covidtestreason='COHORT_SCREENING' THEN 'SCREENING'
                                       WHEN rec._covidtestreason='OUTBREAK_INVESTIGATION_SCREENING' THEN 'OUTBREAK'
                                       WHEN rec._covidtestreason='AFTER_CONTACT_TRACING' THEN 'CONTACT_TO_CASE'
                                       ELSE rec._covidtestreason
                    END;

                SELECT id as _id FROM samples where associatedcase_id = rec._caseid and deleted = false order by sampledatetime DESC limit 1 INTO latest_sample;

                IF latest_sample IS NULL THEN
                    INSERT INTO samples (id, uuid, creationdate, changedate, associatedcase_id, samplepurpose, sampledatetime, reportdatetime, reportinguser_id, samplematerial, samplematerialtext, comment,
                                         deleted, shipped, received,
                                         samplingreason, samplingreasondetails)
                    values (nextval('entity_seq'),
                            overlay(overlay(overlay(
                                                    substring(upper(REPLACE(CAST(CAST(md5(CAST(random() AS text) || CAST(clock_timestamp() AS text)) AS uuid) AS text), '-', '')), 0, 30)
                                                    placing '-' from 7) placing '-' from 14) placing '-' from 21),
                            now(), now(),
                            rec._caseid, 'INTERNAL', rec._reportdate, rec._reportdate, rec._reportinguser_id, 'OTHER', 'Unknown', '[System] Automatically generated sample due to covid test reason migration',
                            false, false, false,
                            _samplingreason, rec._covidtestreasondetails);
                ELSE
                    UPDATE samples set samplingreason = _samplingreason, samplingreasondetails = rec._covidtestreasondetails where id = latest_sample._id;
                END IF;
            END LOOP;
    END;
$$ LANGUAGE plpgsql;

ALTER TABLE cases
    DROP COLUMN covidtestreason,
    DROP COLUMN covidtestreasondetails;

INSERT INTO schema_version (version_number, comment) VALUES (344, 'Add a "sampling reason" field in the sample #4555');

-- 2021-03-03 Introduce disease properties to switch between basic and extended classification #4218
ALTER TABLE diseaseconfiguration ADD COLUMN extendedClassification boolean DEFAULT false;
ALTER TABLE diseaseconfiguration ADD COLUMN extendedClassificationMulti boolean DEFAULT false;

ALTER TABLE diseaseconfiguration_history ADD COLUMN extendedClassification boolean;
ALTER TABLE diseaseconfiguration_history ADD COLUMN extendedClassificationMulti boolean;

UPDATE diseaseconfiguration SET extendedClassification = false WHERE disease not in ('CORONAVIRUS', 'MEASLES');
UPDATE diseaseconfiguration SET extendedClassificationMulti = false WHERE disease not in ('CORONAVIRUS');
UPDATE diseaseconfiguration SET extendedClassification = TRUE WHERE disease in ('CORONAVIRUS', 'MEASLES');
UPDATE diseaseconfiguration SET extendedClassificationMulti = TRUE WHERE disease in ('CORONAVIRUS');

INSERT INTO schema_version (version_number, comment) VALUES (345, 'Introduce disease properties to switch between basic and extended classification #4218');

-- 2021-03-03 [SurvNet Interface] Add "via DEMIS" to pathogen tests #4562
ALTER TABLE pathogentest ADD COLUMN vialims boolean DEFAULT false;
ALTER TABLE pathogentest_history ADD COLUMN vialims boolean DEFAULT false;

INSERT INTO schema_version (version_number, comment) VALUES (346, '[SurvNet Interface] Add "via DEMIS" to pathogen tests #4562');

-- 2021-03-11 [SurvNet Interface] Add additional fields to event clusters #4720
ALTER TABLE events
    ADD COLUMN infectionpathcertainty varchar(255),
    ADD COLUMN humantransmissionmode varchar(255),
    ADD COLUMN parenteraltransmissionmode varchar(255),
    ADD COLUMN medicallyassociatedtransmissionmode varchar(255);

ALTER TABLE events_history
    ADD COLUMN infectionpathcertainty varchar(255),
    ADD COLUMN humantransmissionmode varchar(255),
    ADD COLUMN parenteraltransmissionmode varchar(255),
    ADD COLUMN medicallyassociatedtransmissionmode varchar(255);

INSERT INTO schema_version (version_number, comment) VALUES (347, '[SurvNet Interface] Add additional fields to event clusters #4720');

-- 2021-03-12 [SurvNet Interface] Events > Add new field "Internal ID" #4668
ALTER TABLE events ADD COLUMN internalid text;
ALTER TABLE events_history ADD COLUMN internalid text;

INSERT INTO schema_version (version_number, comment) VALUES (348, '[SurvNet Interface] Events > Add new field "Internal ID" #4668');

-- 2020-02-19 Person contact details #2744
create table personcontactdetail(
     id bigint not null,
     uuid varchar(36) not null unique,
     changedate timestamp not null,
     creationdate timestamp not null,
     person_id bigint not null,
     primarycontact boolean DEFAULT false,
     personcontactdetailtype varchar(255),
     phonenumbertype varchar(255),
     details text,
     contactInformation text,
     additionalInformation text,
     thirdParty boolean DEFAULT false,
     thirdPartyRole text,
     thirdPartyName text
);
ALTER TABLE personcontactdetail OWNER TO sormas_user;

ALTER TABLE personcontactdetail
    ADD CONSTRAINT fk_personcontactdetail_person_id FOREIGN KEY (person_id) REFERENCES person(id);

ALTER TABLE personcontactdetail ADD COLUMN sys_period tstzrange;
CREATE TABLE personcontactdetail_history (LIKE personcontactdetail);
CREATE TRIGGER versioning_trigger
    BEFORE INSERT OR UPDATE OR DELETE ON personcontactdetail
    FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'personcontactdetail_history', true);
ALTER TABLE personcontactdetail_history OWNER TO sormas_user;

CREATE INDEX IF NOT EXISTS idx_personcontactdetail_person_id ON personcontactdetail (person_id);
CREATE INDEX IF NOT EXISTS idx_personcontactdetail_primarycontact ON personcontactdetail (primarycontact);

INSERT INTO personcontactdetail(id, uuid, changedate, creationdate, person_id, primarycontact, personcontactdetailtype, contactinformation, thirdparty)
SELECT nextval('entity_seq'), upper(substring(CAST(CAST(md5(CAST(random() AS text) || CAST(clock_timestamp() AS text)) AS uuid) AS text), 3, 29)), now(), now(), id, true, 'PHONE', phone, false
FROM person WHERE (phone <> '' AND phone IS NOT NULL) IS TRUE AND (phoneowner <> '' AND phoneowner IS NOT NULL) IS FALSE;

INSERT INTO personcontactdetail(id, uuid, changedate, creationdate, person_id, primarycontact, personcontactdetailtype, contactinformation, thirdparty)
SELECT nextval('entity_seq'), upper(substring(CAST(CAST(md5(CAST(random() AS text) || CAST(clock_timestamp() AS text)) AS uuid) AS text), 3, 29)), now(), now(), id, true, 'EMAIL', emailaddress, false
FROM person WHERE (emailaddress <> '' AND emailaddress IS NOT NULL) IS TRUE;

INSERT INTO personcontactdetail(id, uuid, changedate, creationdate, person_id, primarycontact, personcontactdetailtype, contactinformation, thirdparty, thirdpartyname)
SELECT nextval('entity_seq'), upper(substring(CAST(CAST(md5(CAST(random() AS text) || CAST(clock_timestamp() AS text)) AS uuid) AS text), 3, 29)), now(), now(), id, false, 'PHONE', phone, true, phoneowner
FROM person WHERE (phone <> '' AND phone IS NOT NULL) IS TRUE AND (phoneowner <> '' AND phoneowner IS NOT NULL) IS TRUE;

INSERT INTO personcontactdetail(id, uuid, changedate, creationdate, person_id, primarycontact, personcontactdetailtype, additionalinformation, thirdparty, thirdpartyrole, thirdpartyname)
SELECT nextval('entity_seq'), upper(substring(CAST(CAST(md5(CAST(random() AS text) || CAST(clock_timestamp() AS text)) AS uuid) AS text), 3, 29)), now(), now(), id, false, 'OTHER', generalpractitionerdetails, true, 'General practitioner', generalpractitionerdetails
FROM person WHERE (generalpractitionerdetails <> '' AND generalpractitionerdetails IS NOT NULL) IS TRUE;

ALTER TABLE person DROP COLUMN phone;
ALTER TABLE person DROP COLUMN phoneowner;
ALTER TABLE person DROP COLUMN emailaddress;
ALTER TABLE person DROP COLUMN generalpractitionerdetails;

INSERT INTO schema_version (version_number, comment) VALUES (349, 'Person contact details #2744');

-- 2021-03-12 Show "sent to SurvNet" including the last Date of sending bellow the Send to SurvNet Button #4771

CREATE TABLE externalshareinfo (
    id bigint NOT NULL,
    uuid varchar(36) not null unique,
    creationdate timestamp without time zone NOT NULL,
    changedate timestamp not null,
    caze_id bigint,
    event_id bigint,
    sender_id bigint,
    status varchar(255),
    primary key(id)
);

ALTER TABLE externalshareinfo OWNER TO sormas_user;
ALTER TABLE externalshareinfo ADD CONSTRAINT fk_externalshareinfo_caze_id FOREIGN KEY (caze_id) REFERENCES cases (id) ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE externalshareinfo ADD CONSTRAINT fk_externalshareinfo_event_id FOREIGN KEY (event_id) REFERENCES events (id) ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE externalshareinfo ADD CONSTRAINT fk_externalshareinfo_sender_id FOREIGN KEY (sender_id) REFERENCES users (id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO schema_version (version_number, comment) VALUES (350, 'Show "sent to SurvNet" including the last Date of sending bellow the Send to SurvNet Button #4771');

-- 2021-03-09 Add fields for intensive care unit to previous hospitalization #4591
ALTER TABLE previoushospitalization ADD COLUMN intensivecareunit varchar(255);
ALTER TABLE previoushospitalization_history ADD COLUMN intensivecareunit varchar(255);
ALTER TABLE previoushospitalization ADD COLUMN intensivecareunitstart timestamp;
ALTER TABLE previoushospitalization_history ADD COLUMN intensivecareunitstart timestamp;
ALTER TABLE previoushospitalization ADD COLUMN intensivecareunitend timestamp;
ALTER TABLE previoushospitalization_history ADD COLUMN intensivecareunitend timestamp;

INSERT INTO schema_version (version_number, comment) VALUES (351, 'Add fields for intensive care unit to previous hospitalization #4591');

-- 2020-03-17 Create continent and subcontinent #4775
CREATE TABLE continent (
                           id bigint NOT NULL,
                           uuid varchar(36) not null unique,
                           creationdate timestamp without time zone NOT NULL,
                           changedate timestamp not null,
                           archived boolean not null default false,
                           defaultname varchar(255) NOT NULL,
                           externalid varchar(255),
                           primary key(id)
);

CREATE TABLE subcontinent (
                              id bigint NOT NULL,
                              uuid varchar(36) not null unique,
                              creationdate timestamp without time zone NOT NULL,
                              changedate timestamp not null,
                              archived boolean not null default false,
                              defaultname varchar(255) NOT NULL,
                              externalid varchar(255),
                              continent_id bigint NOT NULL,
                              primary key(id)
);

ALTER TABLE continent OWNER TO sormas_user;
ALTER TABLE subcontinent OWNER TO sormas_user;

ALTER TABLE subcontinent ADD CONSTRAINT fk_subcontinent_continent_id FOREIGN KEY (continent_id) REFERENCES continent (id);

ALTER TABLE country ADD COLUMN subcontinent_id BIGINT;
ALTER TABLE country ADD CONSTRAINT fk_country_subcontinent_id FOREIGN KEY (subcontinent_id) REFERENCES subcontinent (id);

INSERT INTO schema_version (version_number, comment) VALUES (352, '2020-03-17 Create continent and subcontinent #4775');

-- 2021-03-22 Provide SQL function to generate a UUIDv4 encoded as base32 #4805
DROP FUNCTION IF EXISTS encode_base32(bytea,int);
DROP FUNCTION IF EXISTS generate_base32_uuid();

/** base 32 encoding based on de.symeda.sormas.api.utils.Base32 **/
CREATE FUNCTION encode_base32(bytes bytea, separatorBlockSize int)
    RETURNS text
    LANGUAGE plpgsql
AS $BODY$
DECLARE
    byteCount int;

    alphabet bytea = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ234567'; /* RFC 4648/3548 */
    SHIFT int = 5; /* SHIFT is the number of bits per output character, so the length of the output is the length of the input multiplied by 8/SHIFT, rounded up.*/
    MASK int = 31;

    result text = '';
    outputLength int;
    buffer int;
    next int;
    bitsLeft int;
    pad int;
    index int;

BEGIN
    byteCount = length(bytes);
    outputLength = (byteCount * 8 + SHIFT - 1) / SHIFT;
    if (separatorBlockSize > 0) THEN
        outputLength = outputLength + outputLength / separatorBlockSize - 1;
    END IF;

    buffer = get_byte(bytes,0);
    next = 1;
    bitsLeft = 8;
    while bitsLeft > 0 OR next < byteCount LOOP
            if (bitsLeft < SHIFT) THEN
                if (next < byteCount) THEN
                    buffer = buffer << 8;
                    buffer = buffer | (get_byte(bytes, next) & 255);
                    next = next+1;
                    bitsLeft = bitsLeft + 8;
                ELSE
                    pad = SHIFT - bitsLeft;
                    buffer = buffer << pad;
                    bitsLeft = bitsLeft + pad;
                END IF;
            END IF;

            index = MASK & (buffer >> (bitsLeft - SHIFT));
            bitsLeft = bitsLeft - SHIFT;
            result = result || chr(get_byte(alphabet,index));

            IF (separatorBlockSize > 0
                AND (length(result) + 1) % (separatorBlockSize + 1) = 0
                AND outputLength - length(result) > separatorBlockSize / 2) THEN
                result = result || '-';
            END IF;
        END LOOP;
    RETURN result;
END;
$BODY$;

CREATE FUNCTION generate_base32_uuid()
    RETURNS text
    LANGUAGE plpgsql
AS $BODY$
DECLARE
    uuidBytes bytea;

BEGIN
    uuidBytes = gen_random_bytes(16);
    uuidBytes = set_byte(uuidBytes, 6, (get_byte(uuidBytes,6) & 15) | 64);  /* clear version, set to version 4 */
    uuidBytes = set_byte(uuidBytes, 8, (get_byte(uuidBytes,8) & 63) | 128);  /* clear variant, set to to IETF variant */
    return encode_base32(uuidBytes, 6);
END;
$BODY$;

INSERT INTO schema_version (version_number, comment) VALUES (353, 'Provide SQL function to generate a UUIDv4 encoded as base32 #4805');

-- 2021-03-17 Add a country field to regions #4784
ALTER TABLE region ADD COLUMN country_id bigint;
ALTER TABLE region ADD CONSTRAINT fk_region_country_id FOREIGN KEY (country_id) REFERENCES country (id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO schema_version (version_number, comment, upgradeNeeded) VALUES (354, 'Add a country field to regions #4784', true);

-- 2021-03-22 [SurvNet Interface] Add checkbox "probable infection environment" to exposures
ALTER TABLE exposures ADD COLUMN probableinfectionenvironment boolean DEFAULT false;
ALTER TABLE exposures_history ADD COLUMN probableinfectionenvironment boolean DEFAULT false;

INSERT INTO schema_version (version_number, comment) VALUES (355, '[SurvNet Interface] Add checkbox "probable infection environment" to exposures');

-- 2021-03-19 Add continent and subcontinent to location #4777

ALTER TABLE location ADD COLUMN continent_id BIGINT;
ALTER TABLE location ADD COLUMN subcontinent_id BIGINT;
ALTER TABLE location ADD CONSTRAINT fk_location_continent_id FOREIGN KEY (continent_id) REFERENCES continent (id);
ALTER TABLE location ADD CONSTRAINT fk_location_subcontinent_id FOREIGN KEY (subcontinent_id) REFERENCES subcontinent (id);
ALTER TABLE location_history ADD COLUMN continent_id BIGINT;
ALTER TABLE location_history ADD COLUMN subcontinent_id BIGINT;

INSERT INTO schema_version (version_number, comment) VALUES (356, '2020-03-19 Add continent and subcontinent to location #4777');

-- 2021-03-07 Add Community Reference to PopulationData entity #4271
ALTER TABLE populationdata ADD COLUMN community_id bigint;
ALTER TABLE populationdata ADD CONSTRAINT fk_populationdata_community_id FOREIGN KEY (community_id) REFERENCES community(id);
ALTER TABLE community ADD COLUMN growthRate real;
INSERT INTO schema_version (version_number, comment) VALUES (357, 'Add Community reference to PopulationData entity #4271');

-- 2021-04-06 Add date and responsible user of last follow-up status change #4138
ALTER TABLE cases ADD COLUMN followupstatuschangedate timestamp without time zone;
ALTER TABLE cases ADD COLUMN followupstatuschangeuser_id BIGINT;
ALTER TABLE contact ADD COLUMN followupstatuschangedate timestamp without time zone;
ALTER TABLE contact ADD COLUMN followupstatuschangeuser_id BIGINT;
ALTER TABLE cases_history ADD COLUMN followupstatuschangedate timestamp without time zone;
ALTER TABLE cases_history ADD COLUMN followupstatuschangeuser_id BIGINT;
ALTER TABLE contact_history ADD COLUMN followupstatuschangedate timestamp without time zone;
ALTER TABLE contact_history ADD COLUMN followupstatuschangeuser_id BIGINT;
ALTER TABLE cases ADD CONSTRAINT fk_cases_followupstatuschangeuser_id FOREIGN KEY (followupstatuschangeuser_id) REFERENCES users (id);
ALTER TABLE contact ADD CONSTRAINT fk_contact_followupstatuschangeuser_id FOREIGN KEY (followupstatuschangeuser_id) REFERENCES users (id);
ALTER TABLE cases_history ADD CONSTRAINT fk_cases_followupstatuschangeuser_id FOREIGN KEY (followupstatuschangeuser_id) REFERENCES users (id);
ALTER TABLE contact_history ADD CONSTRAINT fk_contact_followupstatuschangeuser_id FOREIGN KEY (followupstatuschangeuser_id) REFERENCES users (id);

INSERT INTO schema_version (version_number, comment) VALUES (358, '2021-04-06 Add date and responsible user of last follow-up status change #4138');

-- 2021-04-12 [DEMIS Interface] Introduce option to reject lab messages #4851

ALTER TABLE labmessage ADD COLUMN status varchar(255);

UPDATE labmessage SET status = CASE WHEN processed=true THEN 'PROCESSED'
                                    ELSE 'UNPROCESSED'
                                END;
ALTER TABLE labmessage
    ALTER COLUMN status SET NOT NULL,
    DROP COLUMN processed;

INSERT INTO schema_version (version_number, comment) VALUES (359, '[DEMIS Interface] Introduce option to reject lab messages #4851');

-- 2021-02-18 - Management of EventGroups #4571
CREATE TABLE eventgroups(
    id bigint not null,
    uuid varchar(36) not null unique,
    name text not null,
    changedate timestamp not null,
    creationdate timestamp not null,
    archived boolean not null default false,
    sys_period tstzrange not null,
    PRIMARY KEY (id)
);
ALTER TABLE eventgroups OWNER TO sormas_user;

CREATE TABLE eventgroups_history (LIKE eventgroups);
CREATE TRIGGER versioning_trigger BEFORE INSERT OR UPDATE OR DELETE ON eventgroups
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'eventgroups_history', true);
ALTER TABLE eventgroups_history OWNER TO sormas_user;

CREATE TABLE events_eventgroups(
    event_id bigint not null,
    eventgroup_id bigint not null,
    sys_period tstzrange not null,
    PRIMARY KEY (event_id, eventgroup_id)
);
ALTER TABLE events_eventgroups OWNER TO sormas_user;
ALTER TABLE events_eventgroups ADD CONSTRAINT fk_events_eventgroups_event_id FOREIGN KEY (event_id) REFERENCES events(id);
ALTER TABLE events_eventgroups ADD CONSTRAINT fk_events_eventgroups_eventgroup_id FOREIGN KEY (eventgroup_id) REFERENCES eventgroups(id);

CREATE TABLE events_eventgroups_history (LIKE events_eventgroups);
CREATE TRIGGER versioning_trigger BEFORE INSERT OR UPDATE OR DELETE ON events_eventgroups
FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'events_eventgroups_history', true);
ALTER TABLE events_eventgroups_history OWNER TO sormas_user;

INSERT INTO schema_version (version_number, comment) VALUES (360, 'Management of EventGroups #4571');


-- 2020-04-06 Add contact person details to facilities #4755
ALTER TABLE facility ADD COLUMN contactPersonFirstName text;
ALTER TABLE facility ADD COLUMN contactPersonLastName text;
ALTER TABLE facility ADD COLUMN contactPersonPhone text;
ALTER TABLE facility ADD COLUMN contactPersonEmail text;

ALTER TABLE location ADD COLUMN contactPersonFirstName text;
ALTER TABLE location ADD COLUMN contactPersonLastName text;
ALTER TABLE location ADD COLUMN contactPersonPhone text;
ALTER TABLE location ADD COLUMN contactPersonEmail text;

ALTER TABLE location_history ADD COLUMN contactPersonFirstName text;
ALTER TABLE location_history ADD COLUMN contactPersonLastName text;
ALTER TABLE location_history ADD COLUMN contactPersonPhone text;
ALTER TABLE location_history ADD COLUMN contactPersonEmail text;

INSERT INTO schema_version (version_number, comment) VALUES (361, '#4755 Add contact person details to facilities');

-- 2021-03-26 [DEMIS Interface] visualize respective lab messages in sample and pathogen test sections #4853
ALTER TABLE labmessage ADD COLUMN pathogentest_id BIGINT;
ALTER TABLE labmessage ADD CONSTRAINT fk_labmessage_pathogentest FOREIGN KEY(pathogentest_id) REFERENCES pathogentest(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO schema_version (version_number, comment) VALUES (362, '[DEMIS Interface] visualize respective lab messages in sample and pathogen test sections #4853');

-- 2021-04-20 Change column type of case additional details #5148
ALTER TABLE cases ALTER COLUMN additionaldetails TYPE text;
ALTER TABLE cases_history ALTER COLUMN additionaldetails TYPE text;

INSERT INTO schema_version (version_number, comment) VALUES (363, 'Change column type of case additional details #5148');

-- 2021-04-12 Add additional details to person #3936
ALTER TABLE person ADD COLUMN additionaldetails text;
ALTER TABLE person_history ADD COLUMN additionaldetails text;
INSERT INTO schema_version (version_number, comment) VALUES (364, 'Add additional details to person #3936');

-- 2021-04-15 Add variant specific Nucleic acid detecion methods #5029
ALTER TABLE pathogentest ADD COLUMN pcrtestspecification varchar(255);
ALTER TABLE pathogentest ADD COLUMN testeddiseasevariant_id bigint;
ALTER TABLE pathogentest_history ADD COLUMN pcrtestspecification varchar(255);
ALTER TABLE pathogentest_history ADD COLUMN testeddiseasevariant_id bigint;
ALTER TABLE pathogentest ADD CONSTRAINT fk_pathogentest_diseasevariant_id FOREIGN KEY (testeddiseasevariant_id) REFERENCES diseasevariant(id);

INSERT INTO schema_version (version_number, comment) VALUES (365, '2021-04-15 Add variant specific Nucleic acid detecion methods #5029');

-- 2021-04-23 Decouple Place of stay from the responsible jurisdiction from cases #3254
ALTER TABLE cases
    ADD COLUMN responsibleregion_id BIGINT,
    ADD CONSTRAINT fk_cases_responsibleregion_id FOREIGN KEY (responsibleregion_id) REFERENCES region(id),
    ADD COLUMN responsibledistrict_id BIGINT,
    ADD CONSTRAINT fk_cases_responsibledistrict_id FOREIGN KEY (responsibledistrict_id) REFERENCES district(id),
    ADD COLUMN responsiblecommunity_id BIGINT,
    ADD CONSTRAINT fk_cases_responsiblecommunity_id FOREIGN KEY (responsiblecommunity_id) REFERENCES community(id);

INSERT INTO schema_version (version_number, comment) VALUES (366, 'Decouple Place of stay from the responsible jurisdiction from cases #3254');

-- 2021-04-29 Add evidence fields for event clusters #5061

ALTER TABLE events ADD COLUMN epidemiologicalevidence varchar(255);
ALTER TABLE events ADD COLUMN epidemiologicalevidencedetails json;
ALTER TABLE events ADD COLUMN laboratorydiagnosticEvidence varchar(255);
ALTER TABLE events ADD COLUMN laboratorydiagnosticEvidencedetails json;

INSERT INTO schema_version (version_number, comment) VALUES (367, ' 2021-04-29 Add evidence fields for event clusters #5061');

-- 2021-05-07 Fix equality issue by using jsonb #5061

ALTER TABLE events ALTER COLUMN epidemiologicalevidencedetails set DATA TYPE jsonb using epidemiologicalevidencedetails::jsonb;
ALTER TABLE events ALTER COLUMN laboratorydiagnosticEvidencedetails set DATA TYPE jsonb using laboratorydiagnosticEvidencedetails::jsonb;

INSERT INTO schema_version (version_number, comment) VALUES (368, '2021-05-07 Fix equality issue by using jsonb #5061');

-- 2021-05-07 Move new enum values to screeningType #5063
UPDATE cases SET
    caseidentificationsource = 'SCREENING',
    screeningtype = 'SELF_CONDUCTED_TEST'
WHERE caseidentificationsource = 'SELF_CONDUCTED_TEST';

UPDATE cases SET
    caseidentificationsource = 'SCREENING',
    screeningtype = 'SELF_ARRANGED_TEST'
WHERE caseidentificationsource = 'SELF_ARRANGED_TEST';

INSERT INTO schema_version (version_number, comment) VALUES (369, 'Move new enum values to screeningType #5063');

-- 2021-03-19 Add sample material text to lab message #4773
ALTER TABLE labmessage ADD COLUMN samplematerialtext VARCHAR(255);
ALTER TABLE labmessage_history ADD COLUMN samplematerialtext VARCHAR(255);

INSERT INTO schema_version (version_number, comment) VALUES (370, 'Add sample material text to lab message #4773');

-- 2020-03-03 Add archived to task #3430
ALTER TABLE task ADD COLUMN archived boolean NOT NULL DEFAULT false;
ALTER TABLE task_history ADD COLUMN archived boolean NOT NULL DEFAULT false;

INSERT INTO schema_version (version_number, comment) VALUES (371, 'Add archived to task #3430');

-- 2021-04-29 Add customizable enums #5247
CREATE TABLE customizableenumvalue(
    id bigint not null,
    uuid varchar(36) not null unique,
    datatype varchar(255) not null,
    value text not null,
    caption text not null,
    translations text,
    diseases text,
    description text,
    descriptiontranslations text,
    properties text,
    changedate timestamp not null,
    creationdate timestamp not null,
    sys_period tstzrange not null,
    PRIMARY KEY (id)
);
ALTER TABLE customizableenumvalue OWNER TO sormas_user;

CREATE TABLE customizableenumvalue_history (LIKE customizableenumvalue);
CREATE TRIGGER versioning_trigger BEFORE INSERT OR UPDATE OR DELETE ON customizableenumvalue
    FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'customizableenumvalue_history', true);
ALTER TABLE customizableenumvalue_history OWNER TO sormas_user;

ALTER TABLE cases DROP CONSTRAINT fk_cases_diseasevariant_id;
ALTER TABLE cases RENAME COLUMN diseasevariant_id TO diseasevariant;
ALTER TABLE cases ALTER COLUMN diseasevariant TYPE text USING diseasevariant::text;
ALTER TABLE pathogentest DROP CONSTRAINT fk_pathogentest_diseasevariant_id;
ALTER TABLE pathogentest RENAME COLUMN testeddiseasevariant_id TO testeddiseasevariant;
ALTER TABLE pathogentest ALTER COLUMN testeddiseasevariant TYPE text USING testeddiseasevariant::text;

DO $$
    DECLARE rec RECORD;
    BEGIN
        FOR rec IN SELECT id, disease, name FROM diseasevariant
            LOOP
                INSERT INTO customizableenumvalue(id, uuid, changedate, creationdate, datatype, value, caption, diseases) VALUES (nextval('entity_seq'), generate_base32_uuid(), now(), now(), 'DISEASE_VARIANT', UPPER(REGEXP_REPLACE(rec.name, ' ', '_', 'g')), rec.name, rec.disease);
                UPDATE cases SET diseasevariant = UPPER(REGEXP_REPLACE(rec.name, ' ', '_', 'g')) WHERE diseasevariant = rec.id::text;
                UPDATE pathogentest SET testeddiseasevariant = UPPER(REGEXP_REPLACE(rec.name, ' ', '_', 'g')) WHERE testeddiseasevariant = rec.id::text;
            END LOOP;
    END;
$$ LANGUAGE plpgsql;

DROP TABLE diseasevariant;
DROP TABLE diseasevariant_history;

INSERT INTO schema_version (version_number, comment) VALUES (372, '2021-04-29 Add customizable enums #5247');

-- 2021-03-01 Make contacts mergeable #2409
ALTER TABLE contact ADD COLUMN completeness real;
ALTER TABLE contact_history ADD COLUMN completeness real;

ALTER TABLE contact ADD COLUMN duplicateof_id bigint;
ALTER TABLE contact_history ADD COLUMN duplicateof_id bigint;

ALTER TABLE contact ADD CONSTRAINT fk_contact_duplicateof_id FOREIGN KEY (duplicateof_id) REFERENCES contact(id);

INSERT INTO schema_version (version_number, comment) VALUES (373, 'Make contacts mergeable #2409');

-- 2021-05-19 Indexing by deleted flag on all containing entities should be applied #5465
CREATE INDEX IF NOT EXISTS idx_cases_deleted ON cases (deleted);
CREATE INDEX IF NOT EXISTS idx_contact_deleted ON contact (deleted);
CREATE INDEX IF NOT EXISTS idx_events_deleted ON events (deleted);
CREATE INDEX IF NOT EXISTS idx_samples_deleted ON samples (deleted);
CREATE INDEX IF NOT EXISTS idx_pathogentest_deleted ON pathogentest (deleted);
CREATE INDEX IF NOT EXISTS idx_campaigns_deleted ON campaigns (deleted);
CREATE INDEX IF NOT EXISTS idx_eventparticipant_deleted ON eventparticipant (deleted);
CREATE INDEX IF NOT EXISTS idx_documents_deleted ON documents (deleted);

INSERT INTO schema_version (version_number, comment) VALUES (374, 'Indexing by deleted flag on all containing entities should be applied #5465');

-- 2021-04-30 [SORMAS2SORMAS] accept or reject a shared case from another SORMAS Instance #4423
CREATE TABLE sormastosormassharerequest(
    id bigint not null,
    uuid varchar(36) not null unique,
    changedate timestamp not null,
    creationdate timestamp not null,

    dataType varchar(255),
    status  varchar(255),
    originInfo_id bigint,
    cases json,
    contacts json,
    events json,

    sys_period tstzrange not null,
    PRIMARY KEY (id)
);

ALTER TABLE sormastosormassharerequest OWNER TO sormas_user;
ALTER TABLE sormastosormassharerequest ADD CONSTRAINT fk_sormastosormassharerequest_originInfo_id FOREIGN KEY (originInfo_id) REFERENCES sormastosormasorigininfo (id) ON UPDATE NO ACTION ON DELETE NO ACTION;

CREATE TABLE sormastosormassharerequest_history (LIKE sormastosormassharerequest);
ALTER TABLE sormastosormassharerequest_history OWNER TO sormas_user;
CREATE TRIGGER versioning_trigger BEFORE INSERT OR UPDATE OR DELETE ON sormastosormassharerequest
    FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'sormastosormassharerequest_history', true);

CREATE TABLE sormastosormasshareinfo_entities(
    id bigint not null,
    uuid varchar(36) not null unique,
    changedate timestamp not null,
    creationdate timestamp not null,

    type varchar(255),
    shareinfo_id bigint,
    caze_id bigint,
    contact_id bigint,
    sample_id bigint,
    event_id bigint,
    eventparticipant_id bigint
);
ALTER TABLE sormastosormasshareinfo_entities OWNER TO sormas_user;
ALTER TABLE sormastosormasshareinfo_entities ADD CONSTRAINT fk_sormastosormasshareinfo_entities_shareinfo_id FOREIGN KEY (shareinfo_id) REFERENCES sormastosormasshareinfo (id) ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE sormastosormasshareinfo_entities ADD CONSTRAINT fk_sormastosormasshareinfo_entities_caze_id FOREIGN KEY (caze_id) REFERENCES cases (id) ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE sormastosormasshareinfo_entities ADD CONSTRAINT fk_sormastosormasshareinfo_entities_contact_id FOREIGN KEY (contact_id) REFERENCES contact (id) ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE sormastosormasshareinfo_entities ADD CONSTRAINT fk_sormastosormasshareinfo_entities_sample_id FOREIGN KEY (sample_id) REFERENCES samples (id) ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE sormastosormasshareinfo_entities ADD CONSTRAINT fk_sormastosormasshareinfo_entities_event_id FOREIGN KEY (event_id) REFERENCES events (id) ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE sormastosormasshareinfo_entities ADD CONSTRAINT fk_sormastosormasshareinfo_entities_eventparticipant_id FOREIGN KEY (eventparticipant_id) REFERENCES eventparticipant (id) ON UPDATE NO ACTION ON DELETE NO ACTION;

insert into sormastosormasshareinfo_entities (id, uuid, changedate, creationdate, type, shareinfo_id, caze_id, contact_id, sample_id, event_id, eventparticipant_id)
select nextval('entity_seq'), generate_base32_uuid(), now(), now(),
            CASE WHEN caze_id is not null THEN 'CASE'
            WHEN contact_id is not null THEN 'CONTACT'
            WHEN sample_id is not null THEN 'SAMPLE'
            WHEN event_id is not null THEN 'EVENT'
            WHEN eventparticipant_id is not null THEN 'EVENT_PARTICIPANT'
            ELSE null END, id, caze_id, contact_id, sample_id, event_id, eventparticipant_id from sormastosormasshareinfo;

ALTER TABLE sormastosormasshareinfo
    ADD COLUMN requestUuid varchar(36) unique,
    ADD COLUMN requestStatus varchar(255),
    DROP COLUMN caze_id,
    DROP COLUMN contact_id,
    DROP COLUMN sample_id,
    DROP COLUMN event_id,
    DROP COLUMN eventparticipant_id;

update sormastosormasshareinfo set requestUuid = generate_base32_uuid(), requestStatus = 'ACCEPTED';

ALTER TABLE sormastosormasshareinfo
    ALTER COLUMN requestUuid SET NOT NULL;

INSERT INTO schema_version (version_number, comment) VALUES (375, '[SORMAS2SORMAS] accept or reject a shared case from another SORMAS Instance #4423');

-- 2020-05-26 Introduce an internal token field #5224
ALTER TABLE cases ADD COLUMN internaltoken text;
ALTER TABLE cases_history ADD COLUMN internaltoken text;

ALTER TABLE contact ADD COLUMN internaltoken text;
ALTER TABLE contact_history ADD COLUMN internaltoken text;

ALTER TABLE person ADD COLUMN internaltoken text;
ALTER TABLE person_history ADD COLUMN internaltoken text;

ALTER TABLE events RENAME internalid TO internaltoken;
ALTER TABLE events_history RENAME internalid TO internaltoken;

INSERT INTO schema_version (version_number, comment) VALUES (376, 'Introduce an internal token field #5224');


-- 2021-06-02 Add a checkbox to avoid sending this case to SurvNet #5324
ALTER TABLE cases ADD COLUMN dontsharewithreportingtool boolean DEFAULT false;
ALTER TABLE cases_history ADD COLUMN dontsharewithreportingtool boolean DEFAULT false;

INSERT INTO schema_version (version_number, comment) VALUES (377, 'Add a checkbox to avoid sending this case to SurvNet #5324');

-- 2021-06-22 Set contact with source case known for all existing cases #5841
UPDATE epidata SET contactwithsourcecaseknown = 'YES' FROM cases WHERE cases.epidata_id = epidata.id AND exists (SELECT 1 FROM contact WHERE contact.resultingcase_id = cases.id);

INSERT INTO schema_version (version_number, comment) VALUES (378, 'Set contact with source case known for all existing cases #5841');

-- 2021-06-22 Refine the split of jurisdiction and place of stay #5852
DO $$
    DECLARE _case RECORD;
    BEGIN
        FOR _case IN SELECT * FROM cases WHERE responsibleregion_id IS NULL
                                           AND (reportingdistrict_id IS NULL OR reportingdistrict_id = district_id)
            LOOP
                UPDATE cases
                    SET responsibleregion_id = _case.region_id,
                        responsibledistrict_id = _case.district_id,
                        responsiblecommunity_id = _case.community_id,
                        region_id = null,
                        district_id = null,
                        community_id = null,
                        reportingdistrict_id = null
                where id = _case.id;
            END LOOP;

        FOR _case IN SELECT * FROM cases WHERE responsibleregion_id IS NULL AND reportingdistrict_id IS NOT NULL
            LOOP
                UPDATE cases
                SET responsibleregion_id = (SELECT region_id from district where id = _case.reportingdistrict_id),
                    responsibledistrict_id = _case.reportingdistrict_id,
                    reportingdistrict_id = null
                where id = _case.id;
            END LOOP;
    END;
$$ LANGUAGE plpgsql;

ALTER TABLE cases DROP COLUMN reportingdistrict_id;

INSERT INTO schema_version (version_number, comment) VALUES (379, 'Refine the split of jurisdiction and place of stay #5852');

-- 2021-06-25 [Sormas2Sormas] Returning cases without contacts or samples leaves contacts and samples disabled in both instances #5562
ALTER TABLE sormastosormasorigininfo
    ADD COLUMN withassociatedcontacts boolean DEFAULT false,
    ADD COLUMN withsamples boolean DEFAULT false,
    ADD COLUMN witheventparticipants boolean DEFAULT false;

INSERT INTO schema_version (version_number, comment) VALUES (380, '[Sormas2Sormas] Returning cases without contacts or samples leaves contacts and samples disabled in both instances #5562');

-- 2021-06-29 Immunizations I: Entities, DTOs and backend logic #4756
CREATE TABLE immunization (
                        id bigint not null,
                        uuid varchar(36) not null unique,
                        changedate timestamp not null,
                        creationdate timestamp not null,
                        disease varchar(255) not null,
                        person_id bigint not null,
                        reportdate timestamp not null,
                        reportinguser_id bigint not null,
                        archived boolean DEFAULT false,
                        immunizationstatus varchar(255) not null,
                        meansofimmunization varchar(255) not null,
                        meansOfImmunizationDetails varchar(512),
                        immunizationmanagementstatus varchar(255) not null,
                        externalid varchar(255) not null,
                        responsibleregion_id bigint,
                        responsibledistrict_id bigint,
                        responsiblecommunity_id bigint,
                        country_id bigint,
                        startdate timestamp,
                        enddate timestamp,
                        numberofdoses int,
                        previousinfection varchar(255),
                        lastinfectiondate timestamp,
                        additionaldetails varchar(512),
                        positivetestresultdate timestamp not null,
                        recoverydate timestamp not null,
                        relatedcase_id bigint,
                        deleted boolean DEFAULT false,
                        sys_period tstzrange not null,
                        primary key(id));
ALTER TABLE immunization OWNER TO sormas_user;

ALTER TABLE immunization ADD CONSTRAINT fk_immunization_person_id FOREIGN KEY (person_id) REFERENCES person(id);
ALTER TABLE immunization ADD CONSTRAINT fk_immunization_reportinguser_id FOREIGN KEY (reportinguser_id) REFERENCES users(id);
ALTER TABLE immunization ADD CONSTRAINT fk_immunization_responsibleregion_id FOREIGN KEY (responsibleregion_id) REFERENCES region(id);
ALTER TABLE immunization ADD CONSTRAINT fk_immunization_responsibledistrict_id FOREIGN KEY (responsibledistrict_id) REFERENCES district(id);
ALTER TABLE immunization ADD CONSTRAINT fk_immunization_responsiblecommunity_id FOREIGN KEY (responsiblecommunity_id) REFERENCES community(id);
ALTER TABLE immunization ADD CONSTRAINT fk_immunization_country_id FOREIGN KEY (country_id) REFERENCES country(id);
ALTER TABLE immunization ADD CONSTRAINT fk_immunization_relatedcase_id FOREIGN KEY (relatedcase_id) REFERENCES cases(id);

CREATE INDEX IF NOT EXISTS idx_immunization_deleted ON immunization (deleted);

CREATE TABLE immunization_history (LIKE immunization);
CREATE TRIGGER versioning_trigger
    BEFORE INSERT OR UPDATE OR DELETE ON immunization
    FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'immunization_history', true);
ALTER TABLE immunization_history OWNER TO sormas_user;

INSERT INTO schema_version (version_number, comment) VALUES (381, 'Immunizations I: Entities, DTOs and backend logic #4756');

-- 2021-07-05 DEA TravelEntry entity and backend logic #6022
CREATE TABLE travelentry(
    id bigint not null,
    uuid varchar(36) not null unique,
    changedate timestamp not null,
    creationdate timestamp not null,
    person_id bigint not null,
    reportdate timestamp not null,
    reportinguser_id bigint not null,
    archived boolean DEFAULT false,
    deleted boolean DEFAULT false,
    disease varchar(255) not null,
    diseasedetails text,
    diseasevariant text,
    responsibleregion_id bigint not null,
    responsibledistrict_id bigint not null,
    responsiblecommunity_id bigint,
    pointofentryregion_id bigint,
    pointofentrydistrict_id bigint,
    pointofentry_id bigint not null,
    pointofentrydetails text,
    resultingcase_id bigint,
    externalid varchar(255),
    recovered boolean,
    vaccinated boolean,
    testednegative boolean,
    deacontent text,
    quarantine varchar(255),
    quarantinetypedetails text,
    quarantinefrom timestamp,
    quarantineto timestamp,
    quarantinehelpneeded text,
    quarantineorderedverbally boolean,
    quarantineorderedofficialdocument boolean,
    quarantineorderedverballydate timestamp,
    quarantineorderedofficialdocumentdate timestamp,
    quarantinehomepossible varchar(255),
    quarantinehomepossiblecomment text,
    quarantinehomesupplyensured varchar(255),
    quarantinehomesupplyensuredcomment text,
    quarantineextended boolean DEFAULT false,
    quarantinereduced boolean DEFAULT false,
    quarantineofficialordersent boolean DEFAULT false,
    quarantineofficialordersentdate timestamp,
    sys_period tstzrange not null,
    primary key(id));

ALTER TABLE travelentry OWNER TO sormas_user;

ALTER TABLE travelentry ADD CONSTRAINT fk_travelentry_person_id FOREIGN KEY (person_id) REFERENCES person(id);
ALTER TABLE travelentry ADD CONSTRAINT fk_travelentry_reportinguser_id FOREIGN KEY (reportinguser_id) REFERENCES users(id);
ALTER TABLE travelentry ADD CONSTRAINT fk_travelentry_responsibleregion_id FOREIGN KEY (responsibleregion_id) REFERENCES region(id);
ALTER TABLE travelentry ADD CONSTRAINT fk_travelentry_responsibledistrict_id FOREIGN KEY (responsibledistrict_id) REFERENCES district(id);
ALTER TABLE travelentry ADD CONSTRAINT fk_travelentry_responsiblecommunity_id FOREIGN KEY (responsiblecommunity_id) REFERENCES community(id);
ALTER TABLE travelentry ADD CONSTRAINT fk_travelentry_pointofentryregion_id FOREIGN KEY (pointofentryregion_id) REFERENCES region(id);
ALTER TABLE travelentry ADD CONSTRAINT fk_travelentry_pointofentrydistrict_id FOREIGN KEY (pointofentrydistrict_id) REFERENCES district(id);
ALTER TABLE travelentry ADD CONSTRAINT fk_travelentry_pointofentry_id FOREIGN KEY (pointofentry_id) REFERENCES pointofentry(id);
ALTER TABLE travelentry ADD CONSTRAINT fk_travelentry_resultingcase_id FOREIGN KEY (resultingcase_id) REFERENCES cases(id);

CREATE INDEX IF NOT EXISTS idx_travelentry_deleted ON travelentry (deleted);

CREATE TABLE travelentry_history (LIKE travelentry);
CREATE TRIGGER versioning_trigger
    BEFORE INSERT OR UPDATE OR DELETE ON travelentry
    FOR EACH ROW EXECUTE PROCEDURE versioning('sys_period', 'travelentry_history', true);
ALTER TABLE travelentry_history OWNER TO sormas_user;

INSERT INTO schema_version (version_number, comment) VALUES (382, 'DEA TravelEntry entity and backend logic #6022');

-- 2021-07-12 Add disease variant to event #5525
ALTER TABLE events ADD COLUMN diseasevariant text;
ALTER TABLE events_history ADD COLUMN diseasevariant text;

INSERT INTO schema_version (version_number, comment) VALUES (383, 'Add disease variant to event #5525');

-- 2021-07-15 Immunization data type correction #4756
ALTER TABLE immunization ALTER COLUMN additionaldetails set DATA TYPE text;
ALTER TABLE immunization ALTER COLUMN meansOfImmunizationDetails set DATA TYPE text;
ALTER TABLE immunization_history ALTER COLUMN additionaldetails set DATA TYPE text;
ALTER TABLE immunization_history ALTER COLUMN meansOfImmunizationDetails set DATA TYPE text;

INSERT INTO schema_version (version_number, comment) VALUES (384, 'Immunization data type correction #4756');

-- *** Insert new sql commands BEFORE this line ***
