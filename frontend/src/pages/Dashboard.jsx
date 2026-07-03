import { useAuth } from '../context/AuthContext';
import { useQuery } from '@tanstack/react-query';
import api from '../api/axios';
import { FileText, Clock, CheckCircle, AlertCircle, Plus } from 'lucide-react';
import { Link } from 'react-router-dom';
import {
    PieChart, Pie, Cell, Tooltip, Legend,
    BarChart, Bar, XAxis, YAxis, CartesianGrid, ResponsiveContainer
} from 'recharts';

export default function Dashboard() {
    const { user } = useAuth();

    const { data: complaints = [], isLoading: loading } = useQuery({
        queryKey: ['complaints', user.role, user.departmentId],
        queryFn: async () => {
            const response = await api.get('/complaints');
            return response.data;
        }
    });

    if (loading) return <div>Loading dashboard...</div>;

    const stats = {
        total: complaints.length,
        pending: complaints.filter(c => c.status === 'Submitted' || c.status === 'Assigned').length,
        inProgress: complaints.filter(c => c.status === 'In Progress').length,
        resolved: complaints.filter(c => c.status === 'Resolved' || c.status === 'Closed').length
    };

    // Chart Data Processing for Coordinator/Admin
    const statusCounts = {};
    const categoryCounts = {};
    if (user.role === 'ROLE_DEPARTMENT_COORDINATOR' || user.role === 'ROLE_ADMIN') {
        complaints.forEach(c => {
            statusCounts[c.status] = (statusCounts[c.status] || 0) + 1;
            categoryCounts[c.category] = (categoryCounts[c.category] || 0) + 1;
        });
    }

    const statusData = Object.keys(statusCounts).map(key => ({ name: key, value: statusCounts[key] }));
    const categoryData = Object.keys(categoryCounts).map(key => ({ name: key, value: categoryCounts[key] }));
    
    // Monochromatic Slate/Gray colors instead of bright colors
    const COLORS = ['#1f2937', '#4b5563', '#9ca3af', '#d1d5db', '#e5e7eb'];

    return (
        <div className="space-y-6">
            <h1 className="text-2xl font-bold text-gray-900 tracking-tight">Welcome, {user.username}</h1>
            <p className="text-gray-500 text-sm">Here is an overview of {user.role === 'ROLE_STAFF' ? 'your assigned tasks' : 'your complaints'}.</p>

            <div className="grid grid-cols-2 md:grid-cols-2 lg:grid-cols-4 gap-4">
                <div className="bg-white p-5 rounded-md shadow-sm border border-gray-200 flex flex-col space-y-3">
                    <div className="flex items-center justify-between">
                        <p className="text-sm font-medium text-gray-500">Total Complaints</p>
                        <FileText className="h-4 w-4 text-gray-400" />
                    </div>
                    <p className="text-2xl font-bold text-gray-900">{stats.total}</p>
                </div>
                <div className="bg-white p-5 rounded-md shadow-sm border border-gray-200 flex flex-col space-y-3">
                    <div className="flex items-center justify-between">
                        <p className="text-sm font-medium text-gray-500">Pending</p>
                        <Clock className="h-4 w-4 text-gray-400" />
                    </div>
                    <p className="text-2xl font-bold text-gray-900">{stats.pending}</p>
                </div>
                <div className="bg-white p-5 rounded-md shadow-sm border border-gray-200 flex flex-col space-y-3">
                    <div className="flex items-center justify-between">
                        <p className="text-sm font-medium text-gray-500">In Progress</p>
                        <AlertCircle className="h-4 w-4 text-gray-400" />
                    </div>
                    <p className="text-2xl font-bold text-gray-900">{stats.inProgress}</p>
                </div>
                <div className="bg-white p-5 rounded-md shadow-sm border border-gray-200 flex flex-col space-y-3">
                    <div className="flex items-center justify-between">
                        <p className="text-sm font-medium text-gray-500">Resolved</p>
                        <CheckCircle className="h-4 w-4 text-gray-400" />
                    </div>
                    <p className="text-2xl font-bold text-gray-900">{stats.resolved}</p>
                </div>
            </div>

            {/* Analytics Charts for Manager/Admin */}
            {(user.role === 'ROLE_DEPARTMENT_COORDINATOR' || user.role === 'ROLE_ADMIN') && complaints.length > 0 && (
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
                    <div className="bg-white p-6 rounded-md shadow-sm border border-gray-200">
                        <h2 className="text-sm font-semibold text-gray-900 mb-4">Complaints by Status</h2>
                        <div className="h-64">
                            <ResponsiveContainer width="100%" height="100%">
                                <PieChart>
                                    <Pie
                                        data={statusData}
                                        cx="50%"
                                        cy="50%"
                                        outerRadius={80}
                                        dataKey="value"
                                        label
                                    >
                                        {statusData.map((entry, index) => (
                                            <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                        ))}
                                    </Pie>
                                    <Tooltip />
                                    <Legend />
                                </PieChart>
                            </ResponsiveContainer>
                        </div>
                    </div>
                    <div className="bg-white p-6 rounded-md shadow-sm border border-gray-200">
                        <h2 className="text-sm font-semibold text-gray-900 mb-4">Complaints by Category</h2>
                        <div className="h-64">
                            <ResponsiveContainer width="100%" height="100%">
                                <BarChart data={categoryData}>
                                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e5e7eb" />
                                    <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{fill: '#6b7280', fontSize: 12}} />
                                    <YAxis axisLine={false} tickLine={false} tick={{fill: '#6b7280', fontSize: 12}} />
                                    <Tooltip cursor={{fill: '#f3f4f6'}} />
                                    <Bar dataKey="value" fill="#1f2937" radius={[4, 4, 0, 0]} />
                                </BarChart>
                            </ResponsiveContainer>
                        </div>
                    </div>
                </div>
            )}

            <div className="bg-white rounded-md shadow-sm border border-gray-200 overflow-hidden">
                <div className="px-6 py-4 border-b border-gray-200 bg-gray-50/50 flex justify-between items-center">
                    <h2 className="text-sm font-semibold text-gray-900">
                        {user.role === 'ROLE_REPORTER' ? 'My Complaints' : user.role === 'ROLE_STAFF' ? 'Assigned Tasks' : 'Recent Complaints'}
                    </h2>
                    {user.role === 'ROLE_REPORTER' && (
                        <Link to="/complaints/new" className="bg-gray-900 text-white px-3 py-1.5 text-xs font-medium rounded-md hover:bg-gray-800 transition-colors inline-flex items-center">
                            <Plus className="h-3.5 w-3.5 mr-1" /> New Complaint
                        </Link>
                    )}
                </div>
                <div className="divide-y divide-gray-200">
                    {complaints.length === 0 ? (
                        <div className="p-6 text-center text-sm text-gray-500">No complaints found.</div>
                    ) : (
                        (user.role === 'ROLE_REPORTER' ? complaints : complaints.slice(0, 5)).map(complaint => (
                            <Link to={`/complaints/${complaint.id}`} key={complaint.id} className="p-6 flex items-center justify-between hover:bg-gray-50 transition-colors block">
                                <div>
                                    <h3 className="text-sm font-medium text-gray-900">{complaint.title}</h3>
                                    <p className="text-xs text-gray-500 mt-1">{complaint.category} • {new Date(complaint.dateSubmitted).toLocaleDateString()}</p>
                                </div>
                                <div>
                                    <div className="flex items-center space-x-3">
                                        <span className={`px-2.5 py-0.5 text-xs font-medium rounded-md border ${
                                            complaint.status === 'Resolved' || complaint.status === 'Closed' ? 'bg-gray-50 text-gray-700 border-gray-200' :
                                            complaint.status === 'In Progress' ? 'bg-gray-900 text-white border-gray-900' :
                                            'bg-white text-gray-700 border-gray-300'
                                        }`}>
                                            {complaint.status}
                                        </span>
                                    </div>
                                </div>
                            </Link>
                        ))
                    )}
                </div>
            </div>
        </div>
    );
}
