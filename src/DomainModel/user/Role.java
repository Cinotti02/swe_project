package DomainModel.user;

public enum Role {
    CUSTOMER,
    STAFF,
    OWNER;

    public boolean isCustomer() {
        return this == CUSTOMER;
    }

    public boolean isStaff() {
        return this == STAFF;
    }

    public boolean isOwner() {
        return this == OWNER;
    }
}
