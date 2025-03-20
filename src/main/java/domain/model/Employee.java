package domain.model;


public class Employee {
	
    private final String empId;
    private final String name;
    private final String department;

    public Employee(String id, String name, String department) {
        this.empId = id;
        this.name = name;
        this.department = department;
    }

    public String getEmpId() {
        return empId;
    }

    public String getEmpName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return empId.equals(employee.empId) && name.equals(employee.name) && department.equals(employee.department);
    }
    
    @Override
    public String toString() {
        return "Employee{" +
                "id='" + empId + '\'' +
                ", name='" + name + '\'' +
                ", department='" + department + '\'' +
                '}';
    }
}
