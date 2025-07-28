import React from "react";
import { Link } from "react-router-dom";
import styles from "../../styles/LandingPage.module.css";

const TermsOfServicePage = () => {
  return (
    <div className={styles.landingScrollWrapper} style={{ height: "100vh", overflowY: "auto" }}>
      <div style={{ 
        padding: "40px 20px", 
        maxWidth: "800px", 
        margin: "0 auto",
        lineHeight: "1.8",
        color: "#333"
      }}>
        <h1 style={{ 
          textAlign: "center", 
          marginBottom: "40px", 
          color: "#333",
          fontSize: "28px",
          fontWeight: "bold"
        }}>
          서비스 이용약관
        </h1>
        
        <div style={{ fontSize: "14px" }}>
          <h2 style={{ fontSize: "18px", marginTop: "30px", marginBottom: "15px", color: "#2c3e50" }}>
            제1조(목적)
          </h2>
          <p style={{ marginBottom: "15px" }}>
            본 약관은 chcalendar(이하 '회사')가 제공하는 캘린더 서비스(이하 '서비스')의 이용과 관련하여 회사와 이용자 간의 권리, 의무 및 책임사항, 기타 필요한 사항을 규정함을 목적으로 합니다.
          </p>

          <h2 style={{ fontSize: "18px", marginTop: "30px", marginBottom: "15px", color: "#2c3e50" }}>
            제2조(정의)
          </h2>
          <p style={{ marginBottom: "15px" }}>
            1. '서비스'라 함은 회사가 제공하는 캘린더 관리, 일정 관리, 알림 서비스 등을 의미합니다.<br />
            2. '이용자'라 함은 회사의 서비스에 접속하여 본 약관에 따라 회사와 이용계약을 체결하고 회사가 제공하는 서비스를 이용하는 고객을 의미합니다.<br />
            3. '회원'이라 함은 회사의 서비스에 개인정보를 제공하여 회원등록을 한 자로서, 회사의 정보를 지속적으로 제공받으며, 회사가 제공하는 서비스를 계속적으로 이용할 수 있는 자를 의미합니다.
          </p>

          <h2 style={{ fontSize: "18px", marginTop: "30px", marginBottom: "15px", color: "#2c3e50" }}>
            제3조(약관의 효력 및 변경)
          </h2>
          <p style={{ marginBottom: "15px" }}>
            1. 본 약관은 서비스를 이용하고자 하는 모든 이용자에 대하여 그 효력을 발생합니다.<br />
            2. 회사는 필요한 경우 관련 법령을 위배하지 않는 범위에서 본 약관을 변경할 수 있습니다.<br />
            3. 약관이 변경되는 경우, 회사는 변경사항을 시행일자 7일 전부터 공지사항을 통해 공지합니다.<br />
            4. 이용자가 변경된 약관에 동의하지 않는 경우, 서비스 이용을 중단하고 탈퇴할 수 있습니다.
          </p>

          <h2 style={{ fontSize: "18px", marginTop: "30px", marginBottom: "15px", color: "#2c3e50" }}>
            제4조(서비스의 제공)
          </h2>
          <p style={{ marginBottom: "15px" }}>
            1. 회사는 다음과 같은 서비스를 제공합니다.<br />
            &nbsp;&nbsp;&nbsp;&nbsp;가. 개인 및 그룹 캘린더 관리 서비스<br />
            &nbsp;&nbsp;&nbsp;&nbsp;나. 일정 등록, 수정, 삭제 서비스<br />
            &nbsp;&nbsp;&nbsp;&nbsp;다. 알림 및 리마인더 서비스<br />
            &nbsp;&nbsp;&nbsp;&nbsp;라. Google Calendar 연동 서비스<br />
            &nbsp;&nbsp;&nbsp;&nbsp;마. 기타 회사가 정하는 서비스<br />
            2. 서비스는 연중무휴, 1일 24시간 제공함을 원칙으로 합니다.<br />
            3. 회사는 서비스의 제공에 필요한 경우 정기점검을 실시할 수 있으며, 정기점검시간은 서비스제공화면에 공지한 바에 따릅니다.
          </p>

          <h2 style={{ fontSize: "18px", marginTop: "30px", marginBottom: "15px", color: "#2c3e50" }}>
            제5조(서비스의 중단)
          </h2>
          <p style={{ marginBottom: "15px" }}>
            1. 회사는 컴퓨터 등 정보통신설비의 보수점검, 교체 및 고장, 통신의 두절 등의 사유가 발생한 경우에는 서비스의 제공을 일시적으로 중단할 수 있습니다.<br />
            2. 회사는 제1항의 사유로 서비스의 제공이 일시적으로 중단됨으로 인하여 이용자 또는 제3자가 입은 손해에 대하여 배상합니다. 단, 회사가 고의 또는 과실이 없음을 입증하는 경우에는 그러하지 아니합니다.
          </p>

          <h2 style={{ fontSize: "18px", marginTop: "30px", marginBottom: "15px", color: "#2c3e50" }}>
            제6조(회원가입)
          </h2>
          <p style={{ marginBottom: "15px" }}>
            1. 이용자는 회사가 정한 가입 양식에 따라 회원정보를 기입한 후 본 약관에 동의한다는 의사표시를 함으로서 회원가입을 신청합니다.<br />
            2. 회사는 제1항과 같이 회원으로 가입할 것을 신청한 이용자 중 다음 각호에 해당하지 않는 한 회원으로 등록합니다.<br />
            &nbsp;&nbsp;&nbsp;&nbsp;가. 본 약관에 의하여 이전에 회원자격을 상실한 적이 있는 경우<br />
            &nbsp;&nbsp;&nbsp;&nbsp;나. 등록 내용에 허위, 기재누락, 오기가 있는 경우<br />
            &nbsp;&nbsp;&nbsp;&nbsp;다. 기타 회원으로 등록하는 것이 회사의 기술상 현저히 지장이 있다고 판단되는 경우<br />
            3. 회원가입계약의 성립시기는 회사의 승낙이 회원에게 도달한 시점으로 합니다.
          </p>

          <h2 style={{ fontSize: "18px", marginTop: "30px", marginBottom: "15px", color: "#2c3e50" }}>
            제7조(회원탈퇴 및 자격 상실)
          </h2>
          <p style={{ marginBottom: "15px" }}>
            1. 회원은 회사에 언제든지 탈퇴를 요청할 수 있으며 회사는 즉시 회원탈퇴를 처리합니다.<br />
            2. 회원이 다음 각호의 사유에 해당하는 경우, 회사는 회원자격을 제한 및 정지시킬 수 있습니다.<br />
            &nbsp;&nbsp;&nbsp;&nbsp;가. 가입 신청 시에 허위 내용을 등록한 경우<br />
            &nbsp;&nbsp;&nbsp;&nbsp;나. 다른 사람의 서비스 이용을 방해하거나 그 정보를 도용하는 등 전자상거래 질서를 위협하는 경우<br />
            &nbsp;&nbsp;&nbsp;&nbsp;다. 서비스를 이용하여 법령 또는 이 약관이 금지하거나 공서양속에 반하는 행위를 하는 경우<br />
            3. 회사가 회원자격을 제한, 정지시킨 후, 동일한 행위가 2회 이상 반복되거나 30일 이내에 그 사유가 시정되지 아니하는 경우 회사는 회원자격을 상실시킬 수 있습니다.
          </p>

          <h2 style={{ fontSize: "18px", marginTop: "30px", marginBottom: "15px", color: "#2c3e50" }}>
            제8조(회원의 의무)
          </h2>
          <p style={{ marginBottom: "15px" }}>
            1. 회원은 다음 행위를 하여서는 안됩니다.<br />
            &nbsp;&nbsp;&nbsp;&nbsp;가. 신청 또는 변경 시 허위내용의 등록<br />
            &nbsp;&nbsp;&nbsp;&nbsp;나. 타인의 정보 도용<br />
            &nbsp;&nbsp;&nbsp;&nbsp;다. 회사가 게시한 정보의 변경<br />
            &nbsp;&nbsp;&nbsp;&nbsp;라. 회사가 정한 정보 이외의 정보(컴퓨터 프로그램 등) 등의 송신 또는 게시<br />
            &nbsp;&nbsp;&nbsp;&nbsp;마. 회사 기타 제3자의 저작권 등 지적재산권에 대한 침해<br />
            &nbsp;&nbsp;&nbsp;&nbsp;바. 회사 기타 제3자의 명예를 손상시키거나 업무를 방해하는 행위<br />
            &nbsp;&nbsp;&nbsp;&nbsp;사. 외설 또는 폭력적인 메시지, 화상, 음성, 기타 공서양속에 반하는 정보를 서비스에 공개 또는 게시하는 행위<br />
            2. 회원은 관계법령, 이 약관의 규정, 이용안내 및 서비스상에 공지한 주의사항, 회사가 통지하는 사항 등을 준수하여야 하며, 기타 회사의 업무에 방해되는 행위를 하여서는 안됩니다.
          </p>

          <h2 style={{ fontSize: "18px", marginTop: "30px", marginBottom: "15px", color: "#2c3e50" }}>
            제9조(회사의 의무)
          </h2>
          <p style={{ marginBottom: "15px" }}>
            1. 회사는 법령과 이 약관이 금지하거나 공서양속에 반하는 행위를 하지 않으며 이 약관이 정하는 바에 따라 지속적이고, 안정적으로 서비스를 제공하는데 최선을 다하여야 합니다.<br />
            2. 회사는 이용자가 안전하게 인터넷 서비스를 이용할 수 있도록 이용자의 개인정보(신용정보 포함) 보호를 위한 보안 시스템을 구축하고 개인정보처리방침을 공시하고 준수합니다.<br />
            3. 회사는 서비스이용과 관련하여 이용자로부터 제기된 의견이나 불만이 정당하다고 객관적으로 인정될 경우에는 적절한 절차를 거쳐 즉시 처리하여야 합니다. 다만, 즉시 처리가 곤란한 경우에는 이용자에게 그 사유와 처리일정을 즉시 통보하여야 합니다.
          </p>

          <h2 style={{ fontSize: "18px", marginTop: "30px", marginBottom: "15px", color: "#2c3e50" }}>
            제10조(개인정보보호)
          </h2>
          <p style={{ marginBottom: "15px" }}>
            1. 회사는 이용자의 개인정보를 보호하기 위해 개인정보처리방침을 수립하고 이를 준수합니다.<br />
            2. 이용자의 개인정보 수집, 이용, 제공, 위탁, 파기 등에 관한 자세한 내용은 개인정보처리방침을 통해 확인할 수 있습니다.<br />
            3. 회사는 이용자의 개인정보를 본인의 동의 없이 제3자에게 제공하지 않습니다. 단, 법령의 규정에 의거하거나, 수사 목적으로 법령에 정해진 절차와 방법에 따라 수사기관의 요구가 있는 경우에는 그러하지 아니합니다.
          </p>

          <h2 style={{ fontSize: "18px", marginTop: "30px", marginBottom: "15px", color: "#2c3e50" }}>
            제11조(저작권의 귀속 및 이용제한)
          </h2>
          <p style={{ marginBottom: "15px" }}>
            1. 회사가 작성한 저작물에 대한 저작권 기타 지적재산권은 회사에 귀속합니다.<br />
            2. 이용자는 서비스를 이용함으로써 얻은 정보 중 회사에게 지적재산권이 귀속된 정보를 회사의 사전 승낙 없이 복제, 송신, 출판, 배포, 방송 기타 방법에 의하여 영리목적으로 이용하거나 제3자에게 이용하게 하여서는 안됩니다.<br />
            3. 이용자가 서비스 내에 게시한 콘텐츠의 저작권은 해당 이용자에게 귀속됩니다. 단, 회사는 서비스의 운영, 홍보, 개선 등을 위해 해당 콘텐츠를 서비스 내에서 사용할 수 있는 권리를 가집니다.
          </p>

          <h2 style={{ fontSize: "18px", marginTop: "30px", marginBottom: "15px", color: "#2c3e50" }}>
            제12조(분쟁해결)
          </h2>
          <p style={{ marginBottom: "15px" }}>
            1. 회사는 이용자가 제기하는 정당한 의견이나 불만을 반영하고 그 피해를 보상처리하기 위하여 피해보상처리기구를 설치, 운영합니다.<br />
            2. 회사와 이용자 간에 발생한 전자상거래 분쟁에 관하여는 소비자분쟁조정위원회의 조정에 따를 수 있습니다.
          </p>

          <h2 style={{ fontSize: "18px", marginTop: "30px", marginBottom: "15px", color: "#2c3e50" }}>
            제13조(재판권 및 준거법)
          </h2>
          <p style={{ marginBottom: "15px" }}>
            1. 회사와 이용자 간에 발생한 분쟁에 관하여는 대한민국 법을 적용합니다.<br />
            2. 회사와 이용자 간에 제기된 소송에는 회사의 주소지를 관할하는 법원을 관할법원으로 합니다.
          </p>

          <div style={{ 
            marginTop: "50px", 
            padding: "20px", 
            backgroundColor: "#f8f9fa", 
            borderRadius: "8px",
            textAlign: "center"
          }}>
            <p style={{ marginBottom: "10px", fontSize: "12px", color: "#666" }}>
              본 약관은 2025.07.24.부터 시행됩니다.
            </p>
            <p style={{ fontSize: "12px", color: "#666" }}>
              문의사항: support@chcalendar.site
            </p>
          </div>

          <div style={{ textAlign: "center", marginTop: "40px", marginBottom: "40px" }}>
            <Link to="/" style={{ 
              textDecoration: "none", 
              color: "#007bff", 
              fontSize: "16px",
              fontWeight: "bold",
              padding: "10px 20px",
              border: "2px solid #007bff",
              borderRadius: "5px",
              display: "inline-block"
            }}>
              ← 홈으로 돌아가기
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default TermsOfServicePage; 