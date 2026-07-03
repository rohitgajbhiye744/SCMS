import { useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';
import { Search, Filter, Eye } from 'lucide-react';
import { useQuery, useQueryClient, useMutation } from '@tanstack/react-query';

export default function ComplaintList() {
    const { user } = useAuth();
    const queryClient = useQueryClient();
    const [searchTerm, setSearchTerm] = useState('');
    const [statusFilter, setStatusFilter] = useState('All');
    const [assigningId, setAssigningId] = useState(null);

    const { data: complaints = [], isLoading: loading } = useQuery({
        queryKey: ['complaints', user.role, user.departmentId],
        queryFn: async () => {
            const response = await api.get('/complaints');
            return response.data;
        }
    });

    const { data: staffList = [] } = useQuery({
        queryKey: ['staff', user.departmentId],
        queryFn: async () => {
            const response = await api.get(`/users/staff?departmentId=${user.departmentId}`);
            return response.data;
        },
        enabled: user.role === 'ROLE_DEPARTMENT_COORDINATOR' && !!user.departmentId
    });

    const assignMutation = useMutation({
        mutationFn: async ({ complaintId, staffId }) => {
            await api.patch(`/complaints/${complaintId}/assign?staffId=${staffId}`);
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['complaints'] });
        },
        onSettled: () => {
            setAssigningId(null);
        }
    });

    const handleAssign = async (complaintId, staffId) => {
        if (!staffId) return;
        setAssigningId(complaintId);
        assignMutation.mutate({ complaintId, staffId });
    };

    const filteredComplaints = complaints.filter(c => {
        const matchesSearch = c.title.toLowerCase().includes(searchTerm.toLowerCase()) || 
                              c.id.toString().includes(searchTerm);
        const matchesStatus = statusFilter === 'All' || c.status === statusFilter;
        return matchesSearch && matchesStatus;
    });

    if (loading) return <div className="p-8">Loading complaints...</div>;

    return (
        <div className="space-y-6">
            <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center space-y-4 sm:space-y-0">
                <h1 className="text-2xl font-bold text-gray-900 tracking-tight">
                    {user.role === 'ROLE_REPORTER' ? 'My Complaints' : user.role === 'ROLE_STAFF' ? 'Assigned Tasks' : 'All Complaints'}
                </h1>
                {user.role === 'ROLE_REPORTER' && (
                    <Link
                        to="/complaints/new"
                        className="bg-gray-900 text-white px-4 py-2 text-sm font-medium rounded-md hover:bg-gray-800 transition-colors"
                    >
                        New Complaint
                    </Link>
                )}
            </div>

            <div className="bg-white p-4 rounded-md shadow-sm border border-gray-200 flex flex-col sm:flex-row gap-4">
                <div className="relative flex-1">
                    <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-5 w-5" />
                    <input
                        type="text"
                        placeholder="Search by ID or Title..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500"
                    />
                </div>
                <div className="relative w-full sm:w-64">
                    <Filter className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-5 w-5" />
                    <select
                        value={statusFilter}
                        onChange={(e) => setStatusFilter(e.target.value)}
                        className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500 appearance-none bg-white"
                    >
                        <option value="All">All Statuses</option>
                        <option value="Submitted">Submitted</option>
                        <option value="Assigned">Assigned</option>
                        <option value="In Progress">In Progress</option>
                        <option value="Resolved">Resolved</option>
                        <option value="Closed">Closed</option>
                    </select>
                </div>
            </div>

            <div className="bg-white rounded-md shadow-sm border border-gray-200 overflow-hidden">
                <div className="overflow-x-auto">
                    <table className="min-w-full divide-y divide-gray-200">
                        <thead className="bg-gray-50">
                            <tr>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Title</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Category</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date</th>
                                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                            </tr>
                        </thead>
                        <tbody className="bg-white divide-y divide-gray-200">
                            {filteredComplaints.length === 0 ? (
                                <tr>
                                    <td colSpan="6" className="px-6 py-8 text-center text-gray-500">
                                        No complaints found.
                                    </td>
                                </tr>
                            ) : (
                                filteredComplaints.map((complaint) => (
                                    <tr key={complaint.id} className="hover:bg-gray-50">
                                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                                            #{complaint.id}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                                            {complaint.title}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                            {complaint.category}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <span className={`px-2.5 py-0.5 inline-flex text-xs leading-5 font-medium rounded-md border ${
                                                complaint.status === 'Resolved' || complaint.status === 'Closed' ? 'bg-gray-50 text-gray-700 border-gray-200' :
                                                complaint.status === 'In Progress' ? 'bg-gray-900 text-white border-gray-900' :
                                                'bg-white text-gray-700 border-gray-300'
                                            }`}>
                                                {complaint.status}
                                            </span>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                            {new Date(complaint.dateSubmitted).toLocaleDateString()}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                                            <div className="flex justify-end items-center space-x-3">
                                                {user.role === 'ROLE_DEPARTMENT_COORDINATOR' && (!complaint.assignedStaffEmail) && (complaint.status !== 'Resolved' && complaint.status !== 'Closed') && (
                                                    <select 
                                                        onChange={(e) => handleAssign(complaint.id, e.target.value)}
                                                        disabled={assigningId === complaint.id}
                                                        className="text-xs border border-gray-300 rounded-md py-1 px-2 focus:outline-none focus:ring-1 focus:ring-gray-900 focus:border-gray-900 bg-white"
                                                    >
                                                        <option value="">Quick Assign...</option>
                                                        {staffList.map(staff => (
                                                            <option key={staff.id} value={staff.id}>{staff.username}</option>
                                                        ))}
                                                    </select>
                                                )}
                                                <Link to={`/complaints/${complaint.id}`} className="text-gray-600 hover:text-gray-900 inline-flex items-center">
                                                    <Eye className="h-4 w-4 mr-1" /> View
                                                </Link>
                                            </div>
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
}
