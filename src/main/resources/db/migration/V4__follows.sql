CREATE TABLE IF NOT EXISTS follows (
  follower_id BIGINT NOT NULL,
  followee_id BIGINT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (follower_id, followee_id),

  -- 역방향 조회(= 나를 팔로우하는 사람들) 최적화
  KEY idx_follows_followee (followee_id),

  CONSTRAINT fk_follows_follower
    FOREIGN KEY (follower_id) REFERENCES users(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_follows_followee
    FOREIGN KEY (followee_id) REFERENCES users(id)
    ON DELETE CASCADE,

  -- 자기 자신 팔로우 금지 (MySQL 8.0.16+)
  CONSTRAINT chk_not_self_follow CHECK (follower_id <> followee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
