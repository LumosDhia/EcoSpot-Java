# 🎫 Environmental Reports (Tickets) Roadmap

This roadmap outlines the features for the Environmental Report (Ticket) module, aligned with the **EcoSpot Web** platform.

## ✅ Phase 1: Core Functionality (Aligned with Web)
- [x] **Entity Refactor**: Tickets now represent environmental reports (Title, Description, Status, Priority, Domain).
- [x] **Enums Sync**: implemented `TicketStatus`, `TicketPriority`, and `ActionDomain` to match Symfony backend.
- [x] **Database Schema**: MySQL table updated with new report tracking fields.
- [x] **Advanced CRUD**: Manage environmental issues specifically.
- [x] **Status & Priority Tracking**: Real-time status updates (Pending -> In Progress -> Completed).

## 🚀 Phase 2: Web Sync & AI
- [ ] **AI Integration**: Implement AI analysis for ticket classification (matched to `AiTicketTaskService`).
- [ ] **NGO Assignment**: Feature to assign specifically to NGO accounts.
- [ ] **Geo-Location**: Mapping integration using Latitude/Longitude coordinates.
- [ ] **Spam Detection**: Mark reports as spam based on AI scores.

## 📈 Phase 3: Reporting & Proof
- [ ] **Completion Proof**: Submitting images and messages as proof of resolution.
- [ ] **Consignes (Instructions)**: Adding granular instructions for each action.
- [ ] **Analytics Dashboard**: Monitoring environmental impact and resolution rates.
