package model;
    public class Customer {
        private int id;
        private String accountNo;
        private String name;
        private String address;
        private String telephone;
        private String email;
        private double totalPurchases;

        public Customer() {}

        public Customer(int id, String accountNo, String name, String address,
                        String telephone, String email, double totalPurchases) {
            this.id = id;
            this.accountNo = accountNo;
            this.name = name;
            this.address = address;
            this.telephone = telephone;
            this.email = email;
            this.totalPurchases = totalPurchases;
        }

        // Getters
        public int getId() {
            return id;
        }

        public String getAccountNo() {
            return accountNo;
        }

        public String getName() {
            return name;
        }

        public String getAddress() {
            return address;
        }

        public String getTelephone() {
            return telephone;
        }

        public String getEmail() {
            return email;
        }

        public double getTotalPurchases() {
            return totalPurchases;
        }

        // Setters
        public void setId(int id) {
            this.id = id;
        }

        public void setAccountNo(String accountNo) {
            this.accountNo = accountNo;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public void setTelephone(String telephone) {
            this.telephone = telephone;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public void setTotalPurchases(double totalPurchases) {
            this.totalPurchases = totalPurchases;
        }

        @Override
        public String toString() {
            return "Customer{id=" + id + ", accountNo='" + accountNo + '\'' +
                    ", name='" + name + '\'' +
                    ", address='" + address + '\'' +
                    ", telephone='" + telephone + '\'' +
                    ", email='" + email + '\'' +
                    ", totalPurchases=" + totalPurchases + '}';
        }
    }


