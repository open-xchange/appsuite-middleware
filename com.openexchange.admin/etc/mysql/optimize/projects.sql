#@(#) projects.sql


ALTER TABLE projects
    ADD PRIMARY KEY (intfield01, cid);

ALTER TABLE backup_projects
    ADD PRIMARY KEY (intfield01, cid);

ALTER TABLE projects_participants
    ADD PRIMARY KEY (intfield01, cid, id, group_id);

ALTER TABLE backup_projects_participants
    ADD PRIMARY KEY (intfield01, cid, id, group_id);

ALTER TABLE projects_milestones
    ADD PRIMARY KEY (intfield01, cid, id);

ALTER TABLE backup_projects_milestones
    ADD PRIMARY KEY (intfield01, cid, id);

ALTER TABLE projects_notes
    ADD PRIMARY KEY (intfield01, cid, note_id);

ALTER TABLE backup_projects_notes
    ADD PRIMARY KEY (intfield01, cid, note_id);

ALTER TABLE projects_phases
    ADD PRIMARY KEY (intfield01, cid, phase_id);

ALTER TABLE backup_projects_phases
    ADD PRIMARY KEY (intfield01, cid, phase_id);

ALTER TABLE projects_tasks
    ADD PRIMARY KEY (intfield01, cid, task_id);

ALTER TABLE backup_projects_tasks
    ADD PRIMARY KEY (intfield01, cid, task_id);

ALTER TABLE projects_dependencies
    ADD PRIMARY KEY (intfield01, cid, id);

ALTER TABLE backup_projects_dependencies
    ADD PRIMARY KEY (intfield01, cid, id);

ALTER TABLE projects_antecessors
    ADD PRIMARY KEY (intfield01, cid, id, antecessor);

ALTER TABLE backup_projects_antecessors
    ADD PRIMARY KEY (intfield01, cid, id, antecessor);

ALTER TABLE projects_puids
    ADD PRIMARY KEY (intfield01, cid, puid);

ALTER TABLE backup_projects_puids
    ADD PRIMARY KEY (intfield01, cid, puid);
