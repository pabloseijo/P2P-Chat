CREATE TABLE usuarios (
                       id INTEGER PRIMARY KEY AUTOINCREMENT,
                       usuario TEXT UNIQUE NOT NULL,
                       contrasena TEXT NOT NULL
);

CREATE TABLE amistades (
                             id INTEGER PRIMARY KEY AUTOINCREMENT,
                             user1_id INTEGER NOT NULL,
                             user2_id INTEGER NOT NULL,
                             FOREIGN KEY (user1_id) REFERENCES users(id),
                             FOREIGN KEY (user2_id) REFERENCES users(id)
);

CREATE TABLE solicitudes_amistad (
                                 id INTEGER PRIMARY KEY AUTOINCREMENT,
                                 from_user_id INTEGER NOT NULL,
                                 to_user_id INTEGER NOT NULL,
                                 status TEXT NOT NULL CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED')),
                                 FOREIGN KEY (from_user_id) REFERENCES users(id),
                                 FOREIGN KEY (to_user_id) REFERENCES users(id)
);
