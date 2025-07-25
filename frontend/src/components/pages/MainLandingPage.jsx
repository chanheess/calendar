import React, { useEffect } from "react";
import { useNavigate, Link } from "react-router-dom";
import styles from "../../styles/LandingPage.module.css";
import HeaderComponent from "../HeaderComponent";

const MainLandingPage = () => {
  const navigate = useNavigate();
  useEffect(() => {
    document.body.classList.add("landing-scroll");
    return () => {
      document.body.classList.remove("landing-scroll");
    };
  }, []);
  return (
    <div className={styles.landingScrollWrapper}>
      <HeaderComponent mode="landing" onSidebarToggle={() => {}} />
      <section className={styles.heroSection}>
        <img src="/logo512.png" alt="서비스 로고" style={{ width: 120, height: 120, marginBottom: 24 }} />
        <div className={styles.title}>chcalendar</div>
        <div className={styles.slogan}>모든 일정을 한 곳에서, 통합 캘린더 서비스</div>
        <div className={styles.description}>
          Google 캘린더, 그룹 캘린더 등 다양한 캘린더를 한 번에 관리하고,<br />
          중요한 일정은 푸시 알림으로 놓치지 마세요.
        </div>
      </section>
      <section className={styles.featureSection}>
        <div className={styles.featureContent}>
          <div className={styles.featureText}>
            <div className={styles.featureTitle}>캘린더 통합 관리</div>
            <div className={styles.featureDesc}>
              여러 플랫폼의 캘린더를 한 번에 연동하여<br />
              내 일정과 팀 일정을 쉽고 편리하게 관리할 수 있습니다.<br />
              <br />
              일정 추가, 수정, 삭제는 물론<br />
              반복 일정, 그룹 일정, 알림까지 모두 지원합니다.
            </div>
          </div>
          <div className={styles.featureImagePlaceholder}>
            <div className={styles.imageBox}>
              서비스 화면 예시 이미지
            </div>
          </div>
        </div>
      </section>
      <section className={styles.featureSection}>
        <div className={styles.featureContent}>
          <div className={styles.featureText}>
            <div className={styles.featureTitle}>그룹 내 공유일정</div>
            <div className={styles.featureDesc}>
              팀, 동아리, 가족 등 다양한 그룹과<br />
              일정을 쉽고 빠르게 공유하세요.<br />
              <br />
              그룹원 모두가 실시간으로 일정을 확인하고,<br />
              변경사항도 즉시 반영되어 협업이 편리합니다.
            </div>
          </div>
          <div className={styles.featureImagePlaceholder}>
            <div className={styles.imageBox}>
              그룹 일정 공유 예시 이미지
            </div>
          </div>
        </div>
      </section>
      <section className={styles.featureSection}>
        <div className={styles.featureContent}>
          <div className={styles.featureImagePlaceholder}>
            <div className={styles.imageBox}>
              푸시 알림 예시 이미지
            </div>
          </div>
          <div className={styles.featureText}>
            <div className={styles.featureTitle}>중요 일정 푸시 알림</div>
            <div className={styles.featureDesc}>
              중요한 일정, 회의 등<br />
              원하는 일정에 대해 실시간 푸시 알림을 받아보세요.<br />
              <br />
              알림 시간, 반복 설정 등 세밀한 알림 관리가 가능합니다.
            </div>
          </div>
        </div>
      </section>
      <footer className={styles.footerSection}>
        &copy; {new Date().getFullYear()} chcalendar<br />
        <Link to="/privacy-policy" className={styles.privacyLink}>
          개인정보 처리방침
        </Link>
      </footer>
    </div>
  );
};

export default MainLandingPage; 