ALTER TABLE expense_request_details
    ADD CONSTRAINT ck_expense_amount_positive CHECK (amount > 0);

ALTER TABLE equipment_request_details
    ADD CONSTRAINT ck_equipment_cost_positive CHECK (estimated_cost > 0);

ALTER TABLE equipment_request_details
    ADD CONSTRAINT ck_equipment_quantity_positive CHECK (quantity > 0);

ALTER TABLE leave_request_details
    ADD CONSTRAINT ck_leave_date_order CHECK (end_date >= start_date);

ALTER TABLE leave_request_details
    ADD CONSTRAINT ck_leave_total_days_range CHECK (total_days BETWEEN 1 AND 60);

ALTER TABLE remote_work_request_details
    ADD CONSTRAINT ck_remote_work_date_order CHECK (end_date >= start_date);

ALTER TABLE approval_steps
    ADD CONSTRAINT ck_approval_step_order_positive CHECK (step_order > 0);
