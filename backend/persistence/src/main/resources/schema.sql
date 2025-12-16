CREATE TABLE IF NOT EXISTS members (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS books (
    id VARCHAR(255) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    loaned_to VARCHAR(255),
    due_date DATE
);

CREATE TABLE IF NOT EXISTS book_reservations (
    book_id VARCHAR(255) NOT NULL,
    position INTEGER NOT NULL,
    member_id VARCHAR(255),
    CONSTRAINT fk_book_reservation_book FOREIGN KEY (book_id) REFERENCES books (id)
);
