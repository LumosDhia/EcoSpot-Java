package tn.esprit.services;

import tn.esprit.interfaces.GlobalInterface;
import tn.esprit.ticket.Ticket;
import tn.esprit.util.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TicketService implements GlobalInterface<Ticket> {
    private Connection cnx;

    public TicketService() {
        cnx = MyConnection.getInstance().getCnx();
    }

    @Override
    public void add(Ticket ticket) {}

    @Override
    public void add2(Ticket ticket) {}

    @Override
    public void delete(Ticket ticket) {}

    @Override
    public void update(Ticket ticket) {}

    @Override
    public List<Ticket> getAll() {
        return new ArrayList<>();
    }
}
