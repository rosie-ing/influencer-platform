-- V4__create_saved_contents_table.sql
-- 추천에서 담아두는 '저장 콘텐츠' 박스

CREATE TABLE saved_contents (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id        BIGINT NOT NULL,             -- 소유자

    -- 추천 결과 스냅샷(필수 최소 필드)
    title          VARCHAR(200) NOT NULL,
    caption_idea   VARCHAR(2000) NULL,
    tags_json      JSON NULL,                   -- ["gardening","spring","reels"] 같은 배열
    season         ENUM('SPRING','SUMMER','FALL','WINTER') NULL,
    media_type     ENUM('IMAGE','VIDEO','REELS') NOT NULL DEFAULT 'IMAGE',

    -- 관리용 메타(선택 입력)
    priority       ENUM('LOW','MEDIUM','HIGH') NOT NULL DEFAULT 'MEDIUM',
    status         ENUM('PLANNED','IN_PROGRESS','DONE') NOT NULL DEFAULT 'PLANNED',
    note           VARCHAR(1000) NULL,
    target_date    DATE NULL,

    -- 중복 저장 방지용(선택): 동일 사용자에게 같은 추천을 여러 번 저장하지 않도록
    content_hash   VARCHAR(64) NULL,

    source         VARCHAR(50) NOT NULL DEFAULT 'AI',  -- AI, EDITOR 등
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_saved_contents_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);

-- 빠른 조회를 위한 인덱스
CREATE INDEX idx_saved_contents_user_created ON saved_contents (user_id, created_at DESC);
CREATE INDEX idx_saved_contents_user_status ON saved_contents (user_id, status, target_date);
CREATE INDEX idx_saved_contents_user_season ON saved_contents (user_id, season, created_at DESC);

-- 선택: 해시 중복 방지 (동일 내용 중복 저장 방지), 필요 없으면 주석 처리
CREATE UNIQUE INDEX uq_saved_contents_user_hash ON saved_contents (user_id, content_hash);
