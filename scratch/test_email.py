import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
import os

# Manual config from .env observations
MAIL_HOST = "smtp.gmail.com"
MAIL_PORT = 465
MAIL_USER = "ecospot076@gmail.com"
MAIL_PASS = "lobuvbdyezvifsco"

def test_email():
    print(f"Testing SMTP connection to {MAIL_HOST}:{MAIL_PORT} as {MAIL_USER}...")
    
    msg = MIMEMultipart()
    msg['From'] = MAIL_USER
    msg['To'] = MAIL_USER # Send to self
    msg['Subject'] = "EcoSpot SMTP Test"
    
    body = "This is a test email to verify SMTP configuration."
    msg.attach(MIMEText(body, 'plain'))
    
    try:
        # Using SSL for port 465
        server = smtplib.SMTP_SSL(MAIL_HOST, MAIL_PORT)
        server.login(MAIL_USER, MAIL_PASS)
        server.send_message(msg)
        server.quit()
        print("✅ Email sent successfully!")
    except Exception as e:
        print(f"❌ Failed to send email: {e}")

if __name__ == "__main__":
    test_email()
