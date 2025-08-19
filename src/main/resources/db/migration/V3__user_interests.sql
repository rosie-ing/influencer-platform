CREATE TABLE IF NOT EXISTS user_interests (
  user_id BIGINT NOT NULL,
  interest_id BIGINT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, interest_id),

  -- 인덱스는 KEY 절로
  KEY idx_ui_user (user_id),
  KEY idx_ui_interest (interest_id),

  CONSTRAINT fk_ui_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_ui_interest
    FOREIGN KEY (interest_id) REFERENCES interests(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
