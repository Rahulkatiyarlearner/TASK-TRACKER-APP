Task Tracker & Reminder Application

A desktop-based Task Tracker and Reminder Application developed in Java using Cursor IDE.
This application helps users efficiently manage their daily tasks, track productivity, and receive reminders for routine activities.

The system provides an intuitive graphical interface where users can create tasks, monitor task duration, manage recurring reminders, and organize completed activities.

Project Overview

The Task Tracker application is designed to improve personal productivity and task management.
It allows users to:

Add and manage daily tasks
Track how much time has passed since task creation
Mark tasks as completed
Delete unnecessary tasks
Schedule reminders for routine activities
Manage recurring reminders (daily, weekly, monthly)
Maintain separate views for active and completed tasks

The application stores task and reminder data locally using a JSON-based state management system.

Key Features
1. Task Management

Users can easily create and manage tasks using the application interface.

Functionalities:
Add new tasks
Delete selected tasks
Mark tasks as completed
View active and completed tasks separately
Automatically assign task numbers
Store task creation date and time
Task Details Displayed:
Task Number
Task Name
Task Creation Date
Time elapsed since task creation
2. Real-Time Time Tracking

One of the core features of the application is its ability to continuously track the duration since a task was created.

Example:
43 days 23 hours 11 minutes passed
8 days 14 hours 6 minutes passed

This feature helps users:

Measure productivity
Identify pending tasks
Monitor task completion timelines
3. Reminder System

The application includes a built-in reminder module for routine activities and recurring schedules.

Reminder Features:
Add reminders
Remove reminders
Daily reminders
Weekly reminders
Monthly reminders
Custom reminder time selection
Example Reminder Schedules:
Weekly on Wednesday at 11:15
Weekly on Friday at 11:20

This makes the application useful for:

Habit tracking
Routine management
Study planning
Fitness schedules
Daily productivity workflows
4. Recurring Schedule Support

Users can configure reminders with different recurrence patterns:

Supported Repeat Modes:
Daily
Weekly
Monthly
Additional Scheduling Controls:
Weekday selection for weekly reminders
Day-of-month selection for monthly reminders
24-hour time format support
5. User Interface

The application provides a clean desktop GUI with organized sections for task and reminder management.

Interface Components:
Task input field
Add/Delete buttons
Reminder scheduling controls
Active/Completed tabs
Task table with detailed columns
Reminder list panel

The interface is designed for simplicity and quick task operations.

Technologies Used
Technology	Purpose
Java	Core application development
Swing / Java GUI Components	Desktop user interface
JSON	Local data storage
Cursor IDE	Development environment
Application Workflow
User enters a task name
Task gets added to the active task list
Application records creation date and time
Real-time duration tracking starts automatically
User can:
Mark task as completed
Delete task
Add reminder schedules
Reminder notifications help maintain routine activities
Data Storage

The application stores all task and reminder information in a local JSON file.

Benefits:
Lightweight storage
Easy backup
Persistent task history
Fast data retrieval

Example storage path:

C:\Users\.task-tracker-app\state.json
Use Cases

This application can be used for:

Daily task management
Study planning
Productivity tracking
Fitness routines
Work schedules
Habit building
Personal goal tracking
Future Enhancements

Possible improvements for future versions:

Notification popups
Sound alerts for reminders
Dark mode UI
Cloud synchronization
Search and filter tasks
Priority levels
Export task reports
Calendar integration
Conclusion

The Task Tracker & Reminder Application is a practical productivity tool built in Java that combines task management, real-time task duration tracking, and recurring reminder scheduling into a single desktop application.

Its simple interface and useful automation features help users stay organized, maintain routines, and improve productivity efficiently.
