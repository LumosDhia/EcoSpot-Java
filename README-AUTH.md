# EcoSpot Authentication & Input Validation (Users Branch)

This document outlines the "controle de saisie" (input validation) rules implemented for the Login and Registration screens in the `users` branch. These rules are synchronized with the EcoSpot Web (Symfony) project.

## 1. Registration (Sign-up) Controls

| Field | Requirement | Validation Rule | Error Message |
|-------|-------------|-----------------|---------------|
| **First Name** | Mandatory | Letters, spaces, hyphens, apo only. Max 100 chars. | *"First name can only contain letters and spaces."* / *"First name cannot be longer than 100 characters."* |
| **Last Name** | Mandatory | Letters, spaces, hyphens, apo only. Max 100 chars. | *"Last name can only contain letters and spaces."* / *"Last name cannot be longer than 100 characters."* |
| **Email** | Mandatory | Valid email format (regex). Must be unique in DB. | *"Please enter a valid email address."* / *"Email already exists."* |
| **Address** | Optional | Maximum 255 characters. | *"Address cannot be longer than 255 characters."* |
| **ZIP Code** | Optional | Must be exactly 5 digits. | *"Postal code must be exactly 5 digits."* |
| **City** | Optional | Letters and spaces only. Max 150 chars. | *"City can only contain letters and spaces."* / *"City cannot be longer than 150 characters."* |
| **Password** | Mandatory | Minimum 6 characters. | *"Your password should be at least 6 characters."* |
| **Confirm Password**| Mandatory | Must match Password field. | *"The password fields must match."* |
| **Terms & Conditions**| Mandatory | Must be checked. | *"You should agree to our terms."* |

## 2. Login (Sign-in) Controls

| Field | Requirement | Validation Rule |
|-------|-------------|-----------------|
| **Email** | Mandatory | Must match a valid email format. |
| **Password** | Mandatory | Cannot be empty. |

## 3. Implementation Details

- **Business Logic**: Centralized in `tn.esprit.services.UserService`.
- **UI Feedback**: Real-time error messages displayed in a dedicated red label within `Login.fxml` and `Register.fxml`.
- **Unit Testing**: 100% coverage of validation edge cases in `tn.esprit.user.ValidationTests`.

---
*Note: This documentation is specific to the `users` branch and the authentication module enhancements.*
