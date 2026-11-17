import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useForm } from 'react-hook-form';
import api from '../api/axios';

export default function Register() {
    const { register: registerUser } = useAuth();
    const navigate = useNavigate();
    const { register, handleSubmit, formState: { errors } } = useForm();
    const [error, setError] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    const [departments, setDepartments] = useState([]);

    useEffect(() => {
        api.get('/departments')
            .then(res => setDepartments(res.data))
            .catch(console.error);
    }, []);

    const onSubmit = async (data) => {
        try {
            setIsLoading(true);
            setError('');
            await registerUser(data.username, data.email, data.password, data.phone, data.role, data.departmentId);
            navigate('/login');
        } catch (err) {
            setError(err.response?.data?.message || 'Registration failed. Please try again.');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-[#FFFBF5] py-12 px-4 sm:px-6 lg:px-8">
            <div className="max-w-md w-full space-y-8 bg-white p-8 rounded-2xl shadow-sm border border-[#C3ACD0]/40">
                <div className="text-center">
                    <div className="inline-flex items-center justify-center w-12 h-12 rounded-2xl bg-[#F7EFE5] border border-[#C3ACD0]/50 mb-3">
                        <span className="w-4 h-4 rounded-full bg-[#674188]"></span>
                    </div>
                    <h2 className="text-2xl font-bold text-[#281B34] tracking-tight">
                        Create an account
                    </h2>
                    <p className="mt-1.5 text-sm text-[#7A668A]">
                        Smart Complaint Management System
                    </p>
                </div>
                <form className="mt-6 space-y-6" onSubmit={handleSubmit(onSubmit)}>
                    {error && (
                        <div className="bg-red-50 border-l-4 border-red-500 p-4 mb-4 rounded-r-lg">
                            <p className="text-sm text-red-700">{error}</p>
                        </div>
                    )}
                    <div className="rounded-md space-y-4">
                        <div>
                            <label className="block text-sm font-medium text-[#281B34]">Username</label>
                            <input
                                type="text"
                                {...register('username', { required: 'Username is required' })}
                                className="appearance-none rounded-lg relative block w-full px-3 py-2 border border-[#C3ACD0]/60 placeholder-[#7A668A] text-[#281B34] focus:outline-none focus:ring-2 focus:ring-[#674188] focus:border-[#674188] sm:text-sm mt-1"
                                placeholder="Choose a username"
                            />
                            {errors.username && <span className="text-xs text-red-500 mt-1 block">{errors.username.message}</span>}
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-[#281B34]">Email</label>
                            <input
                                type="email"
                                {...register('email', { 
                                    required: 'Email is required',
                                    pattern: { value: /^\S+@\S+$/i, message: 'Invalid email address' }
                                })}
                                className="appearance-none rounded-lg relative block w-full px-3 py-2 border border-[#C3ACD0]/60 placeholder-[#7A668A] text-[#281B34] focus:outline-none focus:ring-2 focus:ring-[#674188] focus:border-[#674188] sm:text-sm mt-1"
                                placeholder="yourname@domain.edu"
                            />
                            {errors.email && <span className="text-xs text-red-500 mt-1 block">{errors.email.message}</span>}
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-[#281B34]">Phone</label>
                            <input
                                type="text"
                                {...register('phone')}
                                className="appearance-none rounded-lg relative block w-full px-3 py-2 border border-[#C3ACD0]/60 placeholder-[#7A668A] text-[#281B34] focus:outline-none focus:ring-2 focus:ring-[#674188] focus:border-[#674188] sm:text-sm mt-1"
                                placeholder="e.g., 9876543210"
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-[#281B34]">Password</label>
                            <input
                                type="password"
                                {...register('password', { required: 'Password is required', minLength: { value: 6, message: 'Minimum 6 characters' } })}
                                className="appearance-none rounded-lg relative block w-full px-3 py-2 border border-[#C3ACD0]/60 placeholder-[#7A668A] text-[#281B34] focus:outline-none focus:ring-2 focus:ring-[#674188] focus:border-[#674188] sm:text-sm mt-1"
                                placeholder="Create a secure password"
                            />
                            {errors.password && <span className="text-xs text-red-500 mt-1 block">{errors.password.message}</span>}
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-[#281B34]">Role</label>
                            <select
                                {...register('role')}
                                className="appearance-none rounded-lg relative block w-full px-3 py-2 border border-[#C3ACD0]/60 bg-white text-[#281B34] focus:outline-none focus:ring-2 focus:ring-[#674188] focus:border-[#674188] sm:text-sm mt-1"
                            >
                                <option value="REPORTER">Reporter (Student/Teacher)</option>
                                <option value="DEPARTMENT_COORDINATOR">Department Coordinator</option>
                                <option value="STAFF">Staff</option>
                            </select>
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-[#281B34]">Department</label>
                            <select
                                {...register('departmentId')}
                                className="appearance-none rounded-lg relative block w-full px-3 py-2 border border-[#C3ACD0]/60 bg-white text-[#281B34] focus:outline-none focus:ring-2 focus:ring-[#674188] focus:border-[#674188] sm:text-sm mt-1"
                            >
                                <option value="">Select a Department (Optional for Reporters)</option>
                                {departments.map(dept => (
                                    <option key={dept.id} value={dept.id}>{dept.name}</option>
                                ))}
                            </select>
                        </div>
                    </div>

                    <div>
                        <button
                            type="submit"
                            disabled={isLoading}
                            className="group relative w-full flex justify-center py-2.5 px-4 rounded-lg text-sm font-semibold text-white bg-[#674188] hover:bg-[#543372] focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-[#674188] disabled:opacity-50 transition-colors shadow-sm"
                        >
                            {isLoading ? 'Registering...' : 'Register'}
                        </button>
                    </div>
                    <div className="text-sm text-center">
                        <Link to="/login" className="font-medium text-[#674188] hover:text-[#543372] underline">
                            Already have an account? Sign in
                        </Link>
                    </div>
                </form>
            </div>
        </div>
    );
}
